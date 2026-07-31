package org.springblade.modules.recommendfeedback.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.recommendfeedback.mapper.RecommendFeedbackMapper;
import org.springblade.modules.recommendfeedback.pojo.dto.RecommendFeedbackRequest;
import org.springblade.modules.recommendfeedback.pojo.entity.RecommendFeedbackEntity;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** 推荐行为反馈服务。 */
@Service
@RequiredArgsConstructor
public class RecommendFeedbackService {

	private static final Set<String> EVENT_TYPES = Set.of("IMPRESSION", "CLICK", "DWELL", "VIDEO_COMPLETE", "NOT_INTERESTED");
	private static final Set<String> CONTENT_TYPES = Set.of("CONTENT", "NEWS");

	private final RecommendFeedbackMapper mapper;

	/**
	 * 记录推荐反馈。用户身份只取服务端登录态，客户端不能指定 userId。
	 */
	@Transactional(rollbackFor = Exception.class)
	public void record(RecommendFeedbackRequest request, Long userId) {
		if (userId == null || userId <= 0) throw new ServiceException("请先登录后提交推荐反馈");
		if (request == null) throw new ServiceException("推荐反馈参数不能为空");

		Long contentId = request.getContentId();
		if (contentId == null || contentId <= 0) throw new ServiceException("缺少推荐内容ID");
		String contentType = normalize(request.getContentType(), "CONTENT");
		String eventType = normalize(request.getEventType(), "");
		if (!CONTENT_TYPES.contains(contentType)) throw new ServiceException("推荐内容类型不正确");
		if (!EVENT_TYPES.contains(eventType)) throw new ServiceException("推荐反馈类型不正确");

		String requestId = clean(request.getRequestId(), 64);
		if (Func.isBlank(requestId)) requestId = UUID.randomUUID().toString().replace("-", "");
		RecommendFeedbackEntity event = new RecommendFeedbackEntity();
		event.setRequestId(requestId);
		event.setUserId(userId);
		event.setSessionId(clean(request.getSessionId(), 64));
		event.setContentType(contentType);
		event.setContentId(contentId);
		event.setEventType(eventType);
		event.setDurationMs(parseNonNegativeLong(request.getDurationMs()));
		event.setExtraJson(clean(request.getExtraJson(), 2000));
		event.setOccurredAt(new Date());
		try {
			mapper.insert(event);
		} catch (DuplicateKeyException ignored) {
			// 同一用户同一 requestId 重复上报视为幂等成功。
		}
	}

	public Set<Long> notInterestedContentIds(Long userId) {
		if (userId == null || userId <= 0) return Collections.emptySet();
		List<RecommendFeedbackEntity> records = mapper.selectList(Wrappers.<RecommendFeedbackEntity>lambdaQuery()
			.eq(RecommendFeedbackEntity::getUserId, userId)
			.eq(RecommendFeedbackEntity::getContentType, "CONTENT")
			.eq(RecommendFeedbackEntity::getEventType, "NOT_INTERESTED")
			.eq(RecommendFeedbackEntity::getIsDeleted, 0)
			.orderByDesc(RecommendFeedbackEntity::getOccurredAt)
			.last("limit 500"));
		Set<Long> result = new HashSet<>();
		for (RecommendFeedbackEntity record : records) if (record.getContentId() != null) result.add(record.getContentId());
		return result;
	}

	/** 返回内容行为加权分：点击、停留和播放完成会提高后续同内容权重。 */
	public Map<Long, Double> contentBehaviorScores(Long userId) {
		if (userId == null || userId <= 0) return Collections.emptyMap();
		List<RecommendFeedbackEntity> records = mapper.selectList(Wrappers.<RecommendFeedbackEntity>lambdaQuery()
			.eq(RecommendFeedbackEntity::getUserId, userId)
			.eq(RecommendFeedbackEntity::getContentType, "CONTENT")
			.in(RecommendFeedbackEntity::getEventType, "CLICK", "DWELL", "VIDEO_COMPLETE")
			.eq(RecommendFeedbackEntity::getIsDeleted, 0)
			.orderByDesc(RecommendFeedbackEntity::getOccurredAt)
			.last("limit 1000"));
		Map<Long, Double> result = new HashMap<>();
		for (RecommendFeedbackEntity record : records) {
			if (record.getContentId() == null) continue;
			double score = 0;
			if ("CLICK".equals(record.getEventType())) score = 4;
			else if ("VIDEO_COMPLETE".equals(record.getEventType())) score = 12;
			else if ("DWELL".equals(record.getEventType())) {
				long duration = record.getDurationMs() == null ? 0 : Math.max(record.getDurationMs(), 0);
				score = Math.min(duration / 10000.0, 8);
			}
			result.merge(record.getContentId(), score, Double::sum);
		}
		return result;
	}

	private long parseNonNegativeLong(Long value) {
		return value == null ? 0L : Math.max(value, 0L);
	}

	private String normalize(Object value, String defaultValue) {
		String text = clean(value, 32);
		return Func.isBlank(text) ? defaultValue : text.toUpperCase(Locale.ROOT);
	}

	private String clean(Object value, int maxLength) {
		String text = value == null ? "" : String.valueOf(value).trim();
		return text.length() > maxLength ? text.substring(0, maxLength) : text;
	}
}
