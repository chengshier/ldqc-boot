package org.springblade.modules.contentaudit.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.contentaudit.mapper.ContentAuditTaskMapper;
import org.springblade.modules.contentaudit.pojo.entity.ContentAuditTask;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.service.ContentPublishWorkflowService;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 社区动态微信自动审核编排。
 *
 * <p>文本同步检测，图片和短视频封面异步检测。全部通过才自动发布；明确违规自动驳回；
 * review、接口故障、回调超时和媒体不可访问均保留待审核状态，交由自动重试或运营人员兜底。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicContentAutoAuditService {

	public static final String BIZ_TEXT = "IMG_DETAIL_TEXT";
	public static final String BIZ_MEDIA = "IMG_DETAIL_MEDIA";
	private static final long MEDIA_CALLBACK_TIMEOUT_MINUTES = 35L;
	private static final int MAX_ATTEMPTS = 5;

	private final ContentAuditTaskMapper taskMapper;
	private final IImgDetailService imgDetailService;
	private final WechatContentAuditService wechatAuditService;
	private final ContentPublishWorkflowService contentWorkflowService;

	/** 新发布或修改重提：废弃上一轮审核任务，并从头审核当前快照。 */
	@Async
	public void startSubmissionAsync(Long contentId) {
		try {
			start(contentId, true);
		} catch (Exception exception) {
			log.error("启动动态自动审核失败，contentId={}", contentId, exception);
			markContentForManualReview(contentId, "自动审核启动失败，等待运营人员处理");
		}
	}

	/** 视频封面生成后补交媒体审核，不重置已经通过的文本任务。 */
	@Async
	public void resumeAfterPosterReadyAsync(Long contentId) {
		try {
			start(contentId, false);
		} catch (Exception exception) {
			log.error("恢复动态媒体审核失败，contentId={}", contentId, exception);
			markContentForManualReview(contentId, "视频封面审核启动失败，等待运营人员处理");
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void start(Long contentId, boolean resetPreviousTasks) {
		ImgDetailEntity content = requirePendingContent(contentId);
		if (resetPreviousTasks) {
			invalidatePreviousTasks(contentId);
		}

		ContentAuditTask textTask = ensureTask(content, BIZ_TEXT, Func.toStr(content.getContent(), ""));
		if (isNewOrRetryable(textTask)) {
			processTextTask(textTask);
		}
		ContentAuditTask currentTextTask = taskMapper.selectById(textTask.getId());
		if (currentTextTask != null && (Objects.equals(currentTextTask.getAuditStatus(), WechatContentAuditService.REJECTED)
			|| Objects.equals(currentTextTask.getAuditStatus(), WechatContentAuditService.MANUAL_REQUIRED))) {
			// 文案已明确拒绝或需要人工复核时，不再额外消耗媒体审核额度。
			reconcile(contentId);
			return;
		}

		for (String mediaUrl : resolveAuditableMediaUrls(content)) {
			ContentAuditTask mediaTask = ensureTask(content, BIZ_MEDIA, mediaUrl);
			if (isNewOrRetryable(mediaTask)) {
				submitMediaTask(mediaTask, nextAttempt(mediaTask));
			}
		}
		reconcile(contentId);
	}

	/** 由统一重试任务调用，任务已经被声明为 PROCESSING。 */
	@Transactional(rollbackFor = Exception.class)
	public boolean retryMediaTask(Long taskId, int attemptCount) {
		ContentAuditTask task = taskMapper.selectById(taskId);
		if (task == null || !BIZ_MEDIA.equalsIgnoreCase(task.getBizType())) {
			return false;
		}
		submitMediaTask(task, Math.max(1, attemptCount));
		return true;
	}

	/** 微信异步媒体审核回调。 */
	@Transactional(rollbackFor = Exception.class)
	public boolean handleMediaCallback(String traceId, String suggest, String detailReason, Integer statusCode) {
		if (Func.isBlank(traceId)) {
			return false;
		}
		ContentAuditTask task = taskMapper.selectOne(Wrappers.<ContentAuditTask>lambdaQuery()
			.eq(ContentAuditTask::getBizType, BIZ_MEDIA)
			.eq(ContentAuditTask::getProviderTraceId, traceId.trim())
			.eq(ContentAuditTask::getIsDeleted, 0)
			.orderByDesc(ContentAuditTask::getCreateTime)
			.last("limit 1"));
		if (task == null) {
			log.warn("未找到微信媒体审核回调对应任务，traceId={}", traceId);
			return false;
		}
		ImgDetailEntity content = imgDetailService.getById(task.getBizId());
		if (content == null || !Integer.valueOf(ContentPublishWorkflowService.STATUS_PENDING).equals(content.getStatus())) {
			log.info("动态已经不处于待审核状态，忽略媒体回调，contentId={}，traceId={}", task.getBizId(), traceId);
			return true;
		}

		if (statusCode != null && statusCode != 0) {
			int attempts = task.getAttemptCount() == null ? 1 : task.getAttemptCount();
			boolean exhausted = attempts >= MAX_ATTEMPTS;
			task.setAuditStatus(exhausted
				? WechatContentAuditService.MANUAL_REQUIRED : WechatContentAuditService.RETRY);
			task.setProviderTraceId(null);
			task.setResultCode(exhausted ? "MEDIA_CALLBACK_RETRY_EXHAUSTED" : "MEDIA_CALLBACK_ERROR");
			task.setResultMessage(Func.isNotBlank(detailReason)
				? detailReason.trim() : "微信媒体审核回调异常，status_code=" + statusCode);
			task.setNextRetryTime(exhausted ? null : nextRetryTime(attempts));
			task.setAuditTime(null);
			taskMapper.updateById(task);
			reconcile(task.getBizId());
			return true;
		}

		WechatContentAuditService.AuditResult result = wechatAuditService.fromSuggest(
			suggest, traceId, "动态图片或视频封面");
		task.setAuditStatus(result.status());
		task.setResultCode(resultCode(result.status(), "MEDIA_CALLBACK"));
		task.setResultMessage(Func.isNotBlank(detailReason) ? detailReason.trim() : result.reason());
		task.setNextRetryTime(null);
		task.setAuditTime(new Date());
		taskMapper.updateById(task);
		reconcile(task.getBizId());
		return true;
	}

	/** 当任一动态审核任务被人工处理或重试完成后重新汇总。 */
	@Transactional(rollbackFor = Exception.class)
	public void reconcile(Long contentId) {
		ImgDetailEntity content = imgDetailService.getById(contentId);
		if (content == null || !Integer.valueOf(ContentPublishWorkflowService.STATUS_PENDING).equals(content.getStatus())) {
			return;
		}
		List<ContentAuditTask> tasks = currentTasks(contentId);
		ContentAuditTask rejected = tasks.stream()
			.filter(task -> Objects.equals(task.getAuditStatus(), WechatContentAuditService.REJECTED))
			.findFirst().orElse(null);
		if (rejected != null) {
			contentWorkflowService.audit(contentId, "REJECT",
				Func.toStr(rejected.getResultMessage(), "内容未通过微信内容安全审核"), 0L);
			return;
		}
		boolean manualRequired = tasks.stream().anyMatch(task ->
			Objects.equals(task.getAuditStatus(), WechatContentAuditService.MANUAL_REQUIRED));
		if (manualRequired) {
			markContentForManualReview(contentId, "微信自动审核建议运营人员复核");
			return;
		}
		boolean unfinished = tasks.stream().anyMatch(task ->
			Objects.equals(task.getAuditStatus(), WechatContentAuditService.PROCESSING)
				|| Objects.equals(task.getAuditStatus(), WechatContentAuditService.RETRY));
		if (unfinished || !allRequiredTasksPassed(content, tasks)) {
			return;
		}
		contentWorkflowService.audit(contentId, "PASS", "", 0L);
	}

	private void processTextTask(ContentAuditTask task) {
		int attempt = nextAttempt(task);
		task.setAttemptCount(attempt);
		task.setAuditStatus(WechatContentAuditService.PROCESSING);
		task.setResultCode("TEXT_PROCESSING");
		task.setNextRetryTime(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)));
		taskMapper.updateById(task);

		WechatContentAuditService.AuditResult result = wechatAuditService.auditText(
			task.getUserId(), task.getContentSnapshot(), WechatContentAuditService.SCENE_UGC);
		applyImmediateResult(task, result, attempt, "TEXT");
	}

	private void submitMediaTask(ContentAuditTask task, int attempt) {
		task.setAttemptCount(attempt);
		task.setAuditStatus(WechatContentAuditService.PROCESSING);
		task.setResultCode("MEDIA_SUBMITTING");
		task.setNextRetryTime(new Date(System.currentTimeMillis()
			+ TimeUnit.MINUTES.toMillis(MEDIA_CALLBACK_TIMEOUT_MINUTES)));
		taskMapper.updateById(task);

		WechatContentAuditService.AuditResult result = wechatAuditService.submitMedia(
			task.getUserId(), task.getContentSnapshot(), 2, WechatContentAuditService.SCENE_UGC);
		if (result.status() == WechatContentAuditService.PROCESSING) {
			task.setAuditStatus(WechatContentAuditService.PROCESSING);
			task.setProviderTraceId(result.traceId());
			task.setResultCode("MEDIA_WAIT_CALLBACK");
			task.setResultMessage(result.reason());
			task.setNextRetryTime(new Date(System.currentTimeMillis()
				+ TimeUnit.MINUTES.toMillis(MEDIA_CALLBACK_TIMEOUT_MINUTES)));
			task.setAuditTime(null);
			taskMapper.updateById(task);
			return;
		}
		applyImmediateResult(task, result, attempt, "MEDIA");
	}

	private void applyImmediateResult(ContentAuditTask task,
									  WechatContentAuditService.AuditResult result,
									  int attempt,
									  String prefix) {
		byte status = result.status();
		if (status == WechatContentAuditService.RETRY) {
			boolean exhausted = attempt >= MAX_ATTEMPTS;
			task.setAuditStatus(exhausted
				? WechatContentAuditService.MANUAL_REQUIRED : WechatContentAuditService.RETRY);
			task.setResultCode(exhausted ? prefix + "_RETRY_EXHAUSTED" : prefix + "_RETRY");
			task.setResultMessage(exhausted
				? "自动审核连续失败，等待运营人员处理：" + result.reason() : result.reason());
			task.setNextRetryTime(exhausted ? null : nextRetryTime(attempt));
			task.setAuditTime(null);
		} else {
			task.setAuditStatus(status);
			task.setProviderTraceId(result.traceId());
			task.setResultCode(resultCode(status, prefix));
			task.setResultMessage(result.reason());
			task.setNextRetryTime(null);
			task.setAuditTime(new Date());
		}
		taskMapper.updateById(task);
		reconcile(task.getBizId());
	}

	private ContentAuditTask ensureTask(ImgDetailEntity content, String bizType, String snapshot) {
		ContentAuditTask existing = taskMapper.selectOne(Wrappers.<ContentAuditTask>lambdaQuery()
			.eq(ContentAuditTask::getBizType, bizType)
			.eq(ContentAuditTask::getBizId, content.getId())
			.eq(ContentAuditTask::getContentSnapshot, snapshot)
			.eq(ContentAuditTask::getIsDeleted, 0)
			.last("limit 1"));
		if (existing != null) {
			return existing;
		}
		ContentAuditTask task = new ContentAuditTask();
		task.setTenantId(content.getTenantId());
		task.setBizType(bizType);
		task.setBizId(content.getId());
		task.setUserId(content.getUserId());
		task.setContentSnapshot(snapshot);
		task.setAuditStatus(WechatContentAuditService.PROCESSING);
		task.setAttemptCount(0);
		task.setResultCode("CREATED");
		task.setResultMessage("等待微信自动审核");
		task.setIsDeleted(0);
		taskMapper.insert(task);
		return task;
	}

	private void invalidatePreviousTasks(Long contentId) {
		taskMapper.update(null, Wrappers.<ContentAuditTask>lambdaUpdate()
			.eq(ContentAuditTask::getBizId, contentId)
			.in(ContentAuditTask::getBizType, BIZ_TEXT, BIZ_MEDIA)
			.eq(ContentAuditTask::getIsDeleted, 0)
			.set(ContentAuditTask::getIsDeleted, 1));
	}

	private List<ContentAuditTask> currentTasks(Long contentId) {
		return taskMapper.selectList(Wrappers.<ContentAuditTask>lambdaQuery()
			.eq(ContentAuditTask::getBizId, contentId)
			.in(ContentAuditTask::getBizType, BIZ_TEXT, BIZ_MEDIA)
			.eq(ContentAuditTask::getIsDeleted, 0)
			.orderByAsc(ContentAuditTask::getCreateTime));
	}

	private boolean allRequiredTasksPassed(ImgDetailEntity content, List<ContentAuditTask> tasks) {
		long passedText = tasks.stream().filter(task -> BIZ_TEXT.equalsIgnoreCase(task.getBizType()))
			.filter(task -> Objects.equals(task.getAuditStatus(), WechatContentAuditService.PASSED)).count();
		if (passedText < 1) {
			return false;
		}
		Set<String> expectedMedia = new LinkedHashSet<>(resolveAuditableMediaUrls(content));
		if ("VIDEO".equalsIgnoreCase(content.getMediaType())) {
			if (!"READY".equalsIgnoreCase(Func.toStr(content.getMediaProcessStatus(), "")) || expectedMedia.isEmpty()) {
				return false;
			}
		}
		if (expectedMedia.isEmpty()) {
			return false;
		}
		Set<String> passedMedia = new LinkedHashSet<>();
		for (ContentAuditTask task : tasks) {
			if (BIZ_MEDIA.equalsIgnoreCase(task.getBizType())
				&& Objects.equals(task.getAuditStatus(), WechatContentAuditService.PASSED)) {
				passedMedia.add(Func.toStr(task.getContentSnapshot(), ""));
			}
		}
		return passedMedia.containsAll(expectedMedia);
	}

	private List<String> resolveAuditableMediaUrls(ImgDetailEntity content) {
		Set<String> urls = new LinkedHashSet<>();
		if (content == null) return new ArrayList<>();
		if ("VIDEO".equalsIgnoreCase(content.getMediaType())) {
			addHttpUrl(urls, content.getPosterUrl());
			addHttpUrl(urls, content.getCover());
		} else {
			if (Func.isNotBlank(content.getImgsUrl())) {
				try {
					List<String> imageUrls = JSON.parseArray(content.getImgsUrl(), String.class);
					if (imageUrls != null) imageUrls.forEach(url -> addHttpUrl(urls, url));
				} catch (Exception exception) {
					log.warn("解析动态图片列表失败，contentId={}", content.getId(), exception);
				}
			}
			addHttpUrl(urls, content.getMediaUrl());
			addHttpUrl(urls, content.getCover());
		}
		return new ArrayList<>(urls);
	}

	private void addHttpUrl(Set<String> urls, String value) {
		if (Func.isBlank(value)) return;
		String url = value.trim();
		String lower = url.toLowerCase(Locale.ROOT);
		if (lower.startsWith("http://") || lower.startsWith("https://")) {
			urls.add(url);
		}
	}

	private boolean isNewOrRetryable(ContentAuditTask task) {
		return task != null && (task.getAttemptCount() == null || task.getAttemptCount() == 0
			|| Objects.equals(task.getAuditStatus(), WechatContentAuditService.RETRY));
	}

	private int nextAttempt(ContentAuditTask task) {
		return (task.getAttemptCount() == null ? 0 : task.getAttemptCount()) + 1;
	}

	private Date nextRetryTime(int attemptCount) {
		long[] minutes = {1, 5, 15, 60, 180};
		int index = Math.max(0, Math.min(attemptCount - 1, minutes.length - 1));
		return new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes[index]));
	}

	private String resultCode(byte status, String prefix) {
		if (status == WechatContentAuditService.PASSED) return prefix + "_PASSED";
		if (status == WechatContentAuditService.REJECTED) return prefix + "_REJECTED";
		if (status == WechatContentAuditService.MANUAL_REQUIRED) return prefix + "_MANUAL";
		if (status == WechatContentAuditService.PROCESSING) return prefix + "_PROCESSING";
		return prefix + "_RETRY";
	}

	private ImgDetailEntity requirePendingContent(Long contentId) {
		ImgDetailEntity content = imgDetailService.getById(contentId);
		if (content == null || Objects.equals(content.getIsDeleted(), 1)) {
			throw new IllegalStateException("动态内容不存在");
		}
		if (!Integer.valueOf(ContentPublishWorkflowService.STATUS_PENDING).equals(content.getStatus())) {
			throw new IllegalStateException("动态内容当前不是待审核状态");
		}
		return content;
	}

	private void markContentForManualReview(Long contentId, String reason) {
		if (contentId == null) return;
		imgDetailService.update(Wrappers.<ImgDetailEntity>lambdaUpdate()
			.eq(ImgDetailEntity::getId, contentId)
			.eq(ImgDetailEntity::getStatus, ContentPublishWorkflowService.STATUS_PENDING)
			.set(ImgDetailEntity::getAuditReason, reason));
	}
}
