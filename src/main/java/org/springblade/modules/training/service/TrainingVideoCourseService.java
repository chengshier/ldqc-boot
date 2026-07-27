package org.springblade.modules.training.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springblade.common.utils.RedisUtils;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.trainingaccess.mapper.TrainingAccessMapper;
import org.springblade.modules.trainingaccess.pojo.entity.TrainingAccessEntity;
import org.springblade.modules.trainingchapter.mapper.TrainingChapterMapper;
import org.springblade.modules.trainingchapter.pojo.entity.TrainingChapterEntity;
import org.springblade.modules.traininglesson.mapper.TrainingLessonMapper;
import org.springblade.modules.traininglesson.pojo.entity.TrainingLessonEntity;
import org.springblade.modules.trainingprogress.mapper.TrainingProgressMapper;
import org.springblade.modules.trainingprogress.pojo.entity.TrainingProgressEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 长视频课程目录、播放授权和学习进度服务。
 */
@Service
@RequiredArgsConstructor
public class TrainingVideoCourseService {

	private static final String PLAY_TOKEN_PREFIX = "training:play:";
	private static final int PLAY_TOKEN_TTL_SECONDS = 300;

	private final ITrainingService trainingService;
	private final TrainingChapterMapper chapterMapper;
	private final TrainingLessonMapper lessonMapper;
	private final TrainingAccessMapper accessMapper;
	private final TrainingProgressMapper progressMapper;
	private final RedisUtils redisUtils;

	public Map<String, Object> courseDetail(Long trainingId, Long userId) {
		TrainingEntity course = requirePublishedCourse(trainingId);
		boolean courseAuthorized = !requiresPurchase(course) || hasActiveAccess(userId, trainingId);

		List<TrainingChapterEntity> chapters = chapterMapper.selectList(Wrappers.<TrainingChapterEntity>lambdaQuery()
			.eq(TrainingChapterEntity::getTrainingId, trainingId)
			.eq(TrainingChapterEntity::getStatus, 1)
			.eq(TrainingChapterEntity::getIsDeleted, 0)
			.orderByAsc(TrainingChapterEntity::getSortOrder)
			.orderByAsc(TrainingChapterEntity::getCreateTime));
		List<TrainingLessonEntity> lessons = lessonMapper.selectList(Wrappers.<TrainingLessonEntity>lambdaQuery()
			.eq(TrainingLessonEntity::getTrainingId, trainingId)
			.eq(TrainingLessonEntity::getStatus, 1)
			.eq(TrainingLessonEntity::getIsDeleted, 0)
			.orderByAsc(TrainingLessonEntity::getSortOrder)
			.orderByAsc(TrainingLessonEntity::getCreateTime));

		Map<Long, TrainingProgressEntity> progressMap = loadProgressMap(userId, trainingId);
		Map<Long, List<TrainingLessonEntity>> lessonGroups = lessons.stream()
			.collect(Collectors.groupingBy(TrainingLessonEntity::getChapterId, LinkedHashMap::new, Collectors.toList()));
		List<Map<String, Object>> chapterItems = new ArrayList<>();
		for (TrainingChapterEntity chapter : chapters) {
			Map<String, Object> chapterItem = new LinkedHashMap<>();
			chapterItem.put("id", chapter.getId());
			chapterItem.put("title", chapter.getTitle());
			chapterItem.put("description", chapter.getDescription());
			List<Map<String, Object>> lessonItems = new ArrayList<>();
			for (TrainingLessonEntity lesson : lessonGroups.getOrDefault(chapter.getId(), Collections.emptyList())) {
				lessonItems.add(buildLessonOutline(lesson, courseAuthorized, progressMap.get(lesson.getId())));
			}
			chapterItem.put("lessons", lessonItems);
			chapterItems.add(chapterItem);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", course.getId());
		result.put("title", course.getTitle());
		result.put("coverImage", course.getCoverImage());
		result.put("instructorName", course.getInstructorName());
		result.put("description", course.getDescription());
		result.put("price", course.getPrice());
		result.put("category", course.getCategory());
		result.put("courseType", course.getCourseType());
		result.put("contentMode", course.getContentMode());
		result.put("purchaseRequired", requiresPurchase(course));
		result.put("authorized", courseAuthorized);
		result.put("totalLessons", lessons.size());
		result.put("totalVideoDuration", lessons.stream().mapToInt(item -> safeInt(item.getDurationSeconds())).sum());
		result.put("chapters", chapterItems);
		return result;
	}

	public Map<String, Object> createPlayToken(Long lessonId, Long userId) {
		TrainingLessonEntity lesson = requireReadyLesson(lessonId);
		TrainingEntity course = requirePublishedCourse(lesson.getTrainingId());
		if (!canPlay(course, lesson, userId)) {
			throw new ServiceException("该课时需要购买课程后观看");
		}
		if (Func.isBlank(lesson.getVideoUrl())) {
			throw new ServiceException("课时视频尚未上传");
		}

		String token = UUID.randomUUID().toString().replace("-", "");
		Map<String, Object> payload = new HashMap<>();
		payload.put("lessonId", lesson.getId());
		payload.put("trainingId", lesson.getTrainingId());
		payload.put("userId", userId == null ? 0L : userId);
		redisUtils.set(PLAY_TOKEN_PREFIX + token, JSON.toJSONString(payload), PLAY_TOKEN_TTL_SECONDS);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("lessonId", lesson.getId());
		result.put("trainingId", lesson.getTrainingId());
		result.put("playToken", token);
		result.put("playUrl", "/blade-training/training/video-play?token="
			+ URLEncoder.encode(token, StandardCharsets.UTF_8));
		result.put("expiresIn", PLAY_TOKEN_TTL_SECONDS);
		result.put("posterUrl", lesson.getPosterUrl());
		result.put("durationSeconds", safeInt(lesson.getDurationSeconds()));
		return result;
	}

	public String resolveVideoUrl(String token) {
		if (Func.isBlank(token)) {
			throw new ServiceException("播放令牌不能为空");
		}
		Object value = redisUtils.get(PLAY_TOKEN_PREFIX + token);
		if (value == null) {
			throw new ServiceException("播放链接已过期，请重新进入课时");
		}
		Map<String, Object> payload;
		try {
			payload = JSON.parseObject(String.valueOf(value), Map.class);
		} catch (Exception exception) {
			throw new ServiceException("播放令牌无效");
		}
		Long lessonId = Func.toLong(payload.get("lessonId"));
		TrainingLessonEntity lesson = requireReadyLesson(lessonId);
		if (Func.isBlank(lesson.getVideoUrl())) {
			throw new ServiceException("课时视频不可用");
		}
		return lesson.getVideoUrl();
	}

	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> saveProgress(Long lessonId, Integer progressSeconds, Long userId) {
		if (userId == null || userId <= 0) {
			throw new ServiceException("请先登录后再记录学习进度");
		}
		TrainingLessonEntity lesson = requireReadyLesson(lessonId);
		TrainingEntity course = requirePublishedCourse(lesson.getTrainingId());
		if (!canPlay(course, lesson, userId)) {
			throw new ServiceException("暂无该课时播放权限");
		}
		int duration = Math.max(0, safeInt(lesson.getDurationSeconds()));
		int progress = Math.max(0, safeInt(progressSeconds));
		if (duration > 0) progress = Math.min(progress, duration);
		boolean completed = duration > 0 && (progress >= duration - 10 || progress * 100L / duration >= 90);

		TrainingProgressEntity record = progressMapper.selectOne(Wrappers.<TrainingProgressEntity>lambdaQuery()
			.eq(TrainingProgressEntity::getUserId, userId)
			.eq(TrainingProgressEntity::getLessonId, lessonId)
			.eq(TrainingProgressEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (record == null) {
			record = new TrainingProgressEntity();
			record.setUserId(userId);
			record.setTrainingId(lesson.getTrainingId());
			record.setLessonId(lessonId);
			record.setProgressSeconds(progress);
			record.setDurationSeconds(duration);
			record.setCompleted(completed ? 1 : 0);
			record.setLastPlayAt(new Date());
			record.setStatus(1);
			progressMapper.insert(record);
		} else {
			// 不允许较旧的心跳覆盖更靠后的断点。
			record.setProgressSeconds(Math.max(safeInt(record.getProgressSeconds()), progress));
			record.setDurationSeconds(duration);
			record.setCompleted(Func.equals(record.getCompleted(), 1) || completed ? 1 : 0);
			record.setLastPlayAt(new Date());
			progressMapper.updateById(record);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("lessonId", lessonId);
		result.put("progressSeconds", record.getProgressSeconds());
		result.put("durationSeconds", duration);
		result.put("completed", Func.equals(record.getCompleted(), 1));
		return result;
	}

	public List<TrainingProgressEntity> listProgress(Long trainingId, Long userId) {
		if (userId == null || userId <= 0) return Collections.emptyList();
		return progressMapper.selectList(Wrappers.<TrainingProgressEntity>lambdaQuery()
			.eq(TrainingProgressEntity::getUserId, userId)
			.eq(TrainingProgressEntity::getTrainingId, trainingId)
			.eq(TrainingProgressEntity::getIsDeleted, 0)
			.orderByDesc(TrainingProgressEntity::getLastPlayAt));
	}

	private Map<String, Object> buildLessonOutline(TrainingLessonEntity lesson,
														 boolean courseAuthorized,
														 TrainingProgressEntity progress) {
		boolean trial = Func.equals(lesson.getIsTrial(), 1);
		boolean playable = trial || courseAuthorized;
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("id", lesson.getId());
		item.put("chapterId", lesson.getChapterId());
		item.put("title", lesson.getTitle());
		item.put("lessonType", lesson.getLessonType());
		item.put("posterUrl", lesson.getPosterUrl());
		item.put("durationSeconds", safeInt(lesson.getDurationSeconds()));
		item.put("trial", trial);
		item.put("playable", playable);
		item.put("locked", !playable);
		item.put("mediaProcessStatus", lesson.getMediaProcessStatus());
		item.put("progressSeconds", progress == null ? 0 : safeInt(progress.getProgressSeconds()));
		item.put("completed", progress != null && Func.equals(progress.getCompleted(), 1));
		// 目录接口故意不返回 videoUrl，避免未授权用户直接拿到视频地址。
		return item;
	}

	private TrainingEntity requirePublishedCourse(Long trainingId) {
		TrainingEntity course = trainingService.getById(trainingId);
		if (course == null || Func.equals(course.getIsDeleted(), 1) || !Func.equals(course.getStatus(), 1)) {
			throw new ServiceException("课程不存在或已下架");
		}
		String publishStatus = Func.toStr(course.getPublishStatus(), "PUBLISHED");
		if (!"PUBLISHED".equalsIgnoreCase(publishStatus)) {
			throw new ServiceException("课程尚未发布");
		}
		return course;
	}

	private TrainingLessonEntity requireReadyLesson(Long lessonId) {
		TrainingLessonEntity lesson = lessonMapper.selectById(lessonId);
		if (lesson == null || Func.equals(lesson.getIsDeleted(), 1) || !Func.equals(lesson.getStatus(), 1)) {
			throw new ServiceException("课时不存在或已下架");
		}
		if (!"READY".equalsIgnoreCase(Func.toStr(lesson.getMediaProcessStatus(), "READY"))) {
			throw new ServiceException("课时视频仍在处理中");
		}
		return lesson;
	}

	private boolean canPlay(TrainingEntity course, TrainingLessonEntity lesson, Long userId) {
		return Func.equals(lesson.getIsTrial(), 1) || !requiresPurchase(course)
			|| hasActiveAccess(userId, course.getId());
	}

	private boolean requiresPurchase(TrainingEntity course) {
		return Func.equals(course.getPurchaseRequired(), 1);
	}

	private boolean hasActiveAccess(Long userId, Long trainingId) {
		if (userId == null || userId <= 0) return false;
		Date now = new Date();
		return accessMapper.selectCount(Wrappers.<TrainingAccessEntity>lambdaQuery()
			.eq(TrainingAccessEntity::getUserId, userId)
			.eq(TrainingAccessEntity::getTrainingId, trainingId)
			.eq(TrainingAccessEntity::getAccessStatus, "ACTIVE")
			.eq(TrainingAccessEntity::getIsDeleted, 0)
			.and(wrapper -> wrapper.isNull(TrainingAccessEntity::getValidStartAt)
				.or().le(TrainingAccessEntity::getValidStartAt, now))
			.and(wrapper -> wrapper.isNull(TrainingAccessEntity::getValidEndAt)
				.or().ge(TrainingAccessEntity::getValidEndAt, now))) > 0;
	}

	private Map<Long, TrainingProgressEntity> loadProgressMap(Long userId, Long trainingId) {
		if (userId == null || userId <= 0) return Collections.emptyMap();
		return progressMapper.selectList(Wrappers.<TrainingProgressEntity>lambdaQuery()
			.eq(TrainingProgressEntity::getUserId, userId)
			.eq(TrainingProgressEntity::getTrainingId, trainingId)
			.eq(TrainingProgressEntity::getIsDeleted, 0)).stream()
			.collect(Collectors.toMap(TrainingProgressEntity::getLessonId, item -> item, (left, right) -> left));
	}

	private int safeInt(Object value) {
		return Func.toInt(value, 0);
	}
}
