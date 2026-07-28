package org.springblade.modules.contentaudit.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.comment.pojo.entity.CommentEntity;
import org.springblade.modules.comment.service.ICommentService;
import org.springblade.modules.contentaudit.mapper.ContentAuditTaskMapper;
import org.springblade.modules.contentaudit.pojo.entity.ContentAuditTask;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.news.pojo.entity.NewsEntity;
import org.springblade.modules.news.service.INewsService;
import org.springblade.modules.newscomment.pojo.entity.NewsCommentEntity;
import org.springblade.modules.newscomment.service.INewsCommentService;
import org.springblade.modules.usermessage.pojo.entity.UserMessage;
import org.springblade.modules.usermessage.service.IUserMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ContentAuditTaskServiceImpl extends BaseServiceImpl<ContentAuditTaskMapper, ContentAuditTask> implements IContentAuditTaskService {

	public static final byte PROCESSING = 0;
	public static final byte PASSED = WechatContentAuditService.PASSED;
	public static final byte REJECTED = WechatContentAuditService.REJECTED;
	public static final byte RETRY = WechatContentAuditService.RETRY;
	public static final byte MANUAL_REQUIRED = 4;
	private static final int MAX_ATTEMPTS = 5;

	@Autowired
	private WechatContentAuditService wechatContentAuditService;
	@Autowired
	@Lazy
	private ICommentService commentService;
	@Autowired
	@Lazy
	private INewsCommentService newsCommentService;
	@Autowired
	@Lazy
	private IImgDetailService imgDetailService;
	@Autowired
	@Lazy
	private INewsService newsService;
	@Autowired
	private IUserMessageService userMessageService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int retryDueTasks(int batchSize) {
		int safeBatchSize = Math.max(1, Math.min(batchSize, 100));
		Date now = new Date();
		this.update(Wrappers.<ContentAuditTask>lambdaUpdate()
			.eq(ContentAuditTask::getAuditStatus, RETRY)
			.ge(ContentAuditTask::getAttemptCount, MAX_ATTEMPTS)
			.eq(ContentAuditTask::getIsDeleted, 0)
			.set(ContentAuditTask::getAuditStatus, MANUAL_REQUIRED)
			.set(ContentAuditTask::getNextRetryTime, null)
			.set(ContentAuditTask::getResultMessage, "自动审核连续失败，等待运营人员处理"));

		List<ContentAuditTask> tasks = this.list(Wrappers.<ContentAuditTask>lambdaQuery()
			.eq(ContentAuditTask::getAuditStatus, RETRY)
			.lt(ContentAuditTask::getAttemptCount, MAX_ATTEMPTS)
			.eq(ContentAuditTask::getIsDeleted, 0)
			.and(wrapper -> wrapper.isNull(ContentAuditTask::getNextRetryTime)
				.or().le(ContentAuditTask::getNextRetryTime, now))
			.orderByAsc(ContentAuditTask::getNextRetryTime)
			.orderByAsc(ContentAuditTask::getCreateTime)
			.last("limit " + safeBatchSize));

		int processed = 0;
		for (ContentAuditTask task : tasks) {
			try {
				if (processTask(task.getId(), false)) processed++;
			} catch (Exception exception) {
				log.error("content audit retry failed, taskId={}", task.getId(), exception);
			}
		}
		return processed;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean retryNow(Long taskId) {
		ContentAuditTask task = requireTask(taskId);
		if (!Objects.equals(task.getAuditStatus(), RETRY) && !Objects.equals(task.getAuditStatus(), MANUAL_REQUIRED)) {
			throw new ServiceException("只有重试中或待人工处理的任务可以重新审核");
		}
		return processTask(taskId, true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean manualResolve(Long taskId, String action, String reason) {
		ContentAuditTask task = requireTask(taskId);
		String normalizedAction = Func.toStr(action, "").trim().toUpperCase(Locale.ROOT);
		if (!"PASS".equals(normalizedAction) && !"REJECT".equals(normalizedAction)) {
			throw new ServiceException("人工处理动作仅支持 PASS 或 REJECT");
		}
		byte resultStatus = "PASS".equals(normalizedAction) ? PASSED : REJECTED;
		String resultReason = Func.isBlank(reason)
			? (resultStatus == PASSED ? "运营人员人工审核通过" : "运营人员人工审核拒绝")
			: reason.trim();
		applyBusinessResult(task, resultStatus, resultReason);
		task.setAuditStatus(resultStatus);
		task.setResultCode("MANUAL_" + normalizedAction);
		task.setResultMessage(resultReason);
		task.setNextRetryTime(null);
		task.setAuditTime(new Date());
		return this.updateById(task);
	}

	private boolean processTask(Long taskId, boolean force) {
		ContentAuditTask current = requireTask(taskId);
		if (!force && !Objects.equals(current.getAuditStatus(), RETRY)) return false;
		if (force && !Objects.equals(current.getAuditStatus(), RETRY)
			&& !Objects.equals(current.getAuditStatus(), MANUAL_REQUIRED)) return false;

		int previousAttempts = current.getAttemptCount() == null ? 0 : current.getAttemptCount();
		int nextAttempt = previousAttempts + 1;
		boolean claimed = this.update(Wrappers.<ContentAuditTask>lambdaUpdate()
			.eq(ContentAuditTask::getId, taskId)
			.in(ContentAuditTask::getAuditStatus, RETRY, MANUAL_REQUIRED)
			.eq(ContentAuditTask::getIsDeleted, 0)
			.set(ContentAuditTask::getAuditStatus, PROCESSING)
			.set(ContentAuditTask::getAttemptCount, nextAttempt)
			.set(ContentAuditTask::getNextRetryTime, new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5))));
		if (!claimed) return false;

		ContentAuditTask claimedTask = requireTask(taskId);
		WechatContentAuditService.AuditResult result = wechatContentAuditService.audit(
			claimedTask.getUserId(), claimedTask.getContentSnapshot());
		if (result.status() == RETRY) {
			boolean exhausted = nextAttempt >= MAX_ATTEMPTS;
			claimedTask.setAuditStatus(exhausted ? MANUAL_REQUIRED : RETRY);
			claimedTask.setResultCode(exhausted ? "RETRY_EXHAUSTED" : "PROVIDER_RETRY");
			claimedTask.setResultMessage(exhausted
				? "自动审核连续失败，等待运营人员处理：" + Func.toStr(result.reason(), "审核服务不可用")
				: Func.toStr(result.reason(), "审核服务暂不可用"));
			claimedTask.setNextRetryTime(exhausted ? null : nextRetryTime(nextAttempt));
			claimedTask.setAuditTime(null);
			return this.updateById(claimedTask);
		}

		applyBusinessResult(claimedTask, result.status(), result.reason());
		claimedTask.setAuditStatus(result.status());
		claimedTask.setResultCode(result.status() == PASSED ? "PASSED" : "REJECTED");
		claimedTask.setResultMessage(result.reason());
		claimedTask.setNextRetryTime(null);
		claimedTask.setAuditTime(new Date());
		return this.updateById(claimedTask);
	}

	private void applyBusinessResult(ContentAuditTask task, byte status, String reason) {
		if ("TREND_COMMENT".equalsIgnoreCase(task.getBizType())) {
			applyTrendCommentResult(task, status, reason);
			return;
		}
		if ("NEWS_COMMENT".equalsIgnoreCase(task.getBizType())) {
			applyNewsCommentResult(task, status, reason);
			return;
		}
		throw new ServiceException("暂不支持该审核任务类型：" + task.getBizType());
	}

	private void applyTrendCommentResult(ContentAuditTask task, byte status, String reason) {
		CommentEntity comment = commentService.getById(task.getBizId());
		if (comment == null || Objects.equals(comment.getIsDeleted(), 1)) {
			throw new ServiceException("社区评论记录不存在");
		}
		Date now = new Date();
		boolean changed = commentService.update(Wrappers.<CommentEntity>lambdaUpdate()
			.eq(CommentEntity::getId, comment.getId())
			.eq(CommentEntity::getAuditStatus, RETRY)
			.set(CommentEntity::getAuditStatus, status)
			.set(CommentEntity::getAuditReason, reason)
			.set(CommentEntity::getAuditTime, now));
		if (!changed) return;
		if (status == PASSED) {
			imgDetailService.updateCommentCount(String.valueOf(comment.getMid()), 1);
		} else if (status == REJECTED) {
			sendRejectMessage(task, "社区评论未通过审核", reason);
		}
	}

	private void applyNewsCommentResult(ContentAuditTask task, byte status, String reason) {
		NewsCommentEntity comment = newsCommentService.getById(task.getBizId());
		if (comment == null || Objects.equals(comment.getIsDeleted(), 1)) {
			throw new ServiceException("新闻评论记录不存在");
		}
		Date now = new Date();
		boolean changed = newsCommentService.update(Wrappers.<NewsCommentEntity>lambdaUpdate()
			.eq(NewsCommentEntity::getId, comment.getId())
			.eq(NewsCommentEntity::getCommentStatus, RETRY)
			.set(NewsCommentEntity::getCommentStatus, status)
			.set(NewsCommentEntity::getAuditReason, reason)
			.set(NewsCommentEntity::getAuditTime, now));
		if (!changed) return;
		if (status == PASSED) {
			newsService.update(Wrappers.<NewsEntity>lambdaUpdate()
				.eq(NewsEntity::getId, comment.getNewsId())
				.setSql("comment_count = COALESCE(comment_count, 0) + 1"));
		} else if (status == REJECTED) {
			sendRejectMessage(task, "新闻评论未通过审核", reason);
		}
	}

	private void sendRejectMessage(ContentAuditTask task, String title, String reason) {
		UserMessage message = new UserMessage();
		message.setTenantId(task.getTenantId());
		message.setUserId(task.getUserId());
		message.setMessageType("COMMENT_AUDIT_REJECT");
		message.setTitle(title);
		message.setContent(Func.toStr(reason, "评论内容未通过平台审核"));
		message.setBizType(task.getBizType());
		message.setBizId(task.getBizId());
		message.setReadStatus((byte) 0);
		userMessageService.save(message);
	}

	private ContentAuditTask requireTask(Long taskId) {
		if (taskId == null) throw new ServiceException("缺少审核任务ID");
		ContentAuditTask task = this.getById(taskId);
		if (task == null || Objects.equals(task.getIsDeleted(), 1)) {
			throw new ServiceException("审核任务不存在");
		}
		return task;
	}

	private Date nextRetryTime(int attemptCount) {
		long[] minutes = {1, 5, 15, 60, 180};
		int index = Math.max(0, Math.min(attemptCount - 1, minutes.length - 1));
		return new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes[index]));
	}
}
