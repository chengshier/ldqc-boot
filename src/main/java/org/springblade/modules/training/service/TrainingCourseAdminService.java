package org.springblade.modules.training.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.trainingaccess.mapper.TrainingAccessMapper;
import org.springblade.modules.trainingaccess.pojo.entity.TrainingAccessEntity;
import org.springblade.modules.trainingchapter.mapper.TrainingChapterMapper;
import org.springblade.modules.trainingchapter.pojo.entity.TrainingChapterEntity;
import org.springblade.modules.traininglesson.mapper.TrainingLessonMapper;
import org.springblade.modules.traininglesson.pojo.entity.TrainingLessonEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 面向运营人员的课程章节、课时、发布和授权服务。
 */
@Service
@RequiredArgsConstructor
public class TrainingCourseAdminService {

	private final ITrainingService trainingService;
	private final TrainingChapterMapper chapterMapper;
	private final TrainingLessonMapper lessonMapper;
	private final TrainingAccessMapper accessMapper;
	private final IUserService userService;

	public Map<String, Object> outline(Long trainingId) {
		TrainingEntity course = requireCourse(trainingId);
		List<TrainingChapterEntity> chapters = chapterMapper.selectList(Wrappers.<TrainingChapterEntity>lambdaQuery()
			.eq(TrainingChapterEntity::getTrainingId, trainingId)
			.eq(TrainingChapterEntity::getIsDeleted, 0)
			.orderByAsc(TrainingChapterEntity::getSortOrder)
			.orderByAsc(TrainingChapterEntity::getCreateTime));
		List<TrainingLessonEntity> lessons = lessonMapper.selectList(Wrappers.<TrainingLessonEntity>lambdaQuery()
			.eq(TrainingLessonEntity::getTrainingId, trainingId)
			.eq(TrainingLessonEntity::getIsDeleted, 0)
			.orderByAsc(TrainingLessonEntity::getSortOrder)
			.orderByAsc(TrainingLessonEntity::getCreateTime));
		Map<Long, List<TrainingLessonEntity>> grouped = lessons.stream()
			.collect(Collectors.groupingBy(TrainingLessonEntity::getChapterId));
		List<Map<String, Object>> chapterItems = new ArrayList<>();
		for (TrainingChapterEntity chapter : chapters) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("chapter", chapter);
			item.put("lessons", grouped.getOrDefault(chapter.getId(), Collections.emptyList()));
			chapterItems.add(item);
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("course", course);
		result.put("chapters", chapterItems);
		result.put("lessonCount", lessons.stream().filter(item -> !Func.equals(item.getIsDeleted(), 1)).count());
		result.put("readyLessonCount", lessons.stream().filter(item -> "READY".equalsIgnoreCase(Func.toStr(item.getMediaProcessStatus(), ""))).count());
		result.put("trialLessonCount", lessons.stream().filter(item -> Func.equals(item.getIsTrial(), 1)).count());
		result.put("totalDurationSeconds", lessons.stream().mapToInt(item -> Func.toInt(item.getDurationSeconds(), 0)).sum());
		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingChapterEntity saveChapter(Map<String, Object> body) {
		Long trainingId = Func.toLong(body.get("trainingId"));
		TrainingEntity course = requireCourse(trainingId);
		Long chapterId = Func.toLong(body.get("id"));
		TrainingChapterEntity chapter = chapterId == null ? new TrainingChapterEntity() : chapterMapper.selectById(chapterId);
		if (chapterId != null && (chapter == null || !Objects.equals(chapter.getTrainingId(), trainingId))) {
			throw new ServiceException("章节不存在或不属于当前课程");
		}
		String title = Func.toStr(body.get("title"), "").trim();
		if (Func.isBlank(title)) throw new ServiceException("章节标题不能为空");
		chapter.setTrainingId(course.getId());
		chapter.setTitle(title);
		chapter.setDescription(Func.toStr(body.get("description"), "").trim());
		chapter.setSortOrder(Func.toInt(body.get("sortOrder"), 0));
		chapter.setStatus(Func.toInt(body.get("status"), 1));
		if (chapterId == null) chapterMapper.insert(chapter); else chapterMapper.updateById(chapter);
		markCourseDraft(course);
		return chapter;
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteChapter(Long chapterId) {
		TrainingChapterEntity chapter = chapterMapper.selectById(chapterId);
		if (chapter == null || Func.equals(chapter.getIsDeleted(), 1)) throw new ServiceException("章节不存在");
		long lessonCount = lessonMapper.selectCount(Wrappers.<TrainingLessonEntity>lambdaQuery()
			.eq(TrainingLessonEntity::getChapterId, chapterId)
			.eq(TrainingLessonEntity::getIsDeleted, 0));
		if (lessonCount > 0) throw new ServiceException("章节下仍有课时，请先移动或删除课时");
		chapter.setIsDeleted(1);
		chapterMapper.updateById(chapter);
		markCourseDraft(requireCourse(chapter.getTrainingId()));
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingLessonEntity saveLesson(Map<String, Object> body) {
		Long trainingId = Func.toLong(body.get("trainingId"));
		Long chapterId = Func.toLong(body.get("chapterId"));
		TrainingEntity course = requireCourse(trainingId);
		TrainingChapterEntity chapter = chapterMapper.selectById(chapterId);
		if (chapter == null || Func.equals(chapter.getIsDeleted(), 1) || !Objects.equals(chapter.getTrainingId(), trainingId)) {
			throw new ServiceException("请选择当前课程下的有效章节");
		}
		Long lessonId = Func.toLong(body.get("id"));
		TrainingLessonEntity lesson = lessonId == null ? new TrainingLessonEntity() : lessonMapper.selectById(lessonId);
		if (lessonId != null && (lesson == null || !Objects.equals(lesson.getTrainingId(), trainingId))) {
			throw new ServiceException("课时不存在或不属于当前课程");
		}
		String title = Func.toStr(body.get("title"), "").trim();
		if (Func.isBlank(title)) throw new ServiceException("课时标题不能为空");
		String lessonType = Func.toStr(body.get("lessonType"), "VIDEO").trim().toUpperCase(Locale.ROOT);
		if (!"VIDEO".equals(lessonType) && !"TEXT".equals(lessonType)) throw new ServiceException("课时类型不正确");
		String oldVideoUrl = lesson.getVideoUrl();
		String newVideoUrl = Func.toStr(body.get("videoUrl"), "").trim();
		if ("VIDEO".equals(lessonType) && Func.isBlank(newVideoUrl)) throw new ServiceException("视频课时必须上传视频");

		lesson.setTrainingId(trainingId);
		lesson.setChapterId(chapterId);
		lesson.setTitle(title);
		lesson.setLessonType(lessonType);
		lesson.setVideoUrl(newVideoUrl);
		lesson.setPosterUrl(Func.toStr(body.get("posterUrl"), "").trim());
		lesson.setDurationSeconds(Math.max(0, Func.toInt(body.get("durationSeconds"), 0)));
		lesson.setIsTrial(Func.toInt(body.get("isTrial"), 0) == 1 ? 1 : 0);
		lesson.setSortOrder(Func.toInt(body.get("sortOrder"), 0));
		lesson.setStatus(Func.toInt(body.get("status"), 1));
		if ("TEXT".equals(lessonType)) {
			lesson.setMediaProcessStatus("READY");
		} else if (!Objects.equals(Func.toStr(oldVideoUrl, ""), newVideoUrl)) {
			lesson.setMediaProcessStatus(Func.isNotBlank(lesson.getPosterUrl()) ? "READY" : "PROCESSING");
		} else if (Func.isBlank(lesson.getMediaProcessStatus())) {
			lesson.setMediaProcessStatus(Func.isNotBlank(lesson.getPosterUrl()) ? "READY" : "PROCESSING");
		}
		if (lessonId == null) lessonMapper.insert(lesson); else lessonMapper.updateById(lesson);
		markCourseDraft(course);
		recalculateCourse(trainingId);
		return lesson;
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteLesson(Long lessonId) {
		TrainingLessonEntity lesson = lessonMapper.selectById(lessonId);
		if (lesson == null || Func.equals(lesson.getIsDeleted(), 1)) throw new ServiceException("课时不存在");
		lesson.setIsDeleted(1);
		lessonMapper.updateById(lesson);
		TrainingEntity course = requireCourse(lesson.getTrainingId());
		markCourseDraft(course);
		recalculateCourse(lesson.getTrainingId());
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingEntity publishCourse(Long trainingId) {
		TrainingEntity course = requireCourse(trainingId);
		String mode = normalizeMode(course.getContentMode());
		if ("ONLINE".equals(mode) || "MIXED".equals(mode)) {
			List<TrainingLessonEntity> lessons = lessonMapper.selectList(Wrappers.<TrainingLessonEntity>lambdaQuery()
				.eq(TrainingLessonEntity::getTrainingId, trainingId)
				.eq(TrainingLessonEntity::getStatus, 1)
				.eq(TrainingLessonEntity::getIsDeleted, 0));
			if (lessons.isEmpty()) throw new ServiceException("线上课程至少需要一个已启用课时");
			for (TrainingLessonEntity lesson : lessons) {
				if ("VIDEO".equalsIgnoreCase(lesson.getLessonType())) {
					if (Func.isBlank(lesson.getVideoUrl())) throw new ServiceException("课时“" + lesson.getTitle() + "”尚未上传视频");
					if (!"READY".equalsIgnoreCase(Func.toStr(lesson.getMediaProcessStatus(), ""))) {
						throw new ServiceException("课时“" + lesson.getTitle() + "”的视频尚未处理完成");
					}
				}
			}
		}
		recalculateCourse(trainingId);
		course = requireCourse(trainingId);
		course.setPublishStatus("PUBLISHED");
		course.setAuditReason(null);
		course.setStatus(1);
		trainingService.updateById(course);
		return course;
	}

	@Transactional(rollbackFor = Exception.class)
	public void offlineCourse(Long trainingId, String reason) {
		TrainingEntity course = requireCourse(trainingId);
		if (Func.isBlank(reason)) throw new ServiceException("下架时必须填写原因");
		course.setPublishStatus("OFFLINE");
		course.setAuditReason(reason.trim());
		trainingService.updateById(course);
	}

	public List<Map<String, Object>> searchUsers(String keyword) {
		if (Func.isBlank(keyword)) return Collections.emptyList();
		String value = keyword.trim();
		List<User> users = userService.list(Wrappers.<User>lambdaQuery()
			.eq(User::getIsDeleted, 0)
			.and(wrapper -> wrapper.like(User::getName, value)
				.or().like(User::getRealName, value)
				.or().like(User::getAccount, value)
				.or().like(User::getPhone, value))
			.last("limit 20"));
		return users.stream().map(user -> {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("id", user.getId());
			item.put("name", firstNonBlank(user.getRealName(), user.getName(), user.getAccount(), "用户" + user.getId()));
			item.put("phone", maskPhone(user.getPhone()));
			item.put("avatar", user.getAvatar());
			return item;
		}).collect(Collectors.toList());
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingAccessEntity grantAccess(Map<String, Object> body) {
		Long trainingId = Func.toLong(body.get("trainingId"));
		Long userId = Func.toLong(body.get("userId"));
		requireCourse(trainingId);
		if (userId == null || userService.getById(userId) == null) throw new ServiceException("请选择有效用户");
		TrainingAccessEntity access = accessMapper.selectOne(Wrappers.<TrainingAccessEntity>lambdaQuery()
			.eq(TrainingAccessEntity::getTrainingId, trainingId)
			.eq(TrainingAccessEntity::getUserId, userId)
			.eq(TrainingAccessEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (access == null) {
			access = new TrainingAccessEntity();
			access.setTrainingId(trainingId);
			access.setUserId(userId);
			access.setSourceType(Func.toStr(body.get("sourceType"), "ADMIN"));
			access.setSourceId(Func.toStr(body.get("sourceId"), ""));
			access.setAccessStatus("ACTIVE");
			access.setValidStartAt(toDate(body.get("validStartAt")));
			access.setValidEndAt(toDate(body.get("validEndAt")));
			access.setStatus(1);
			accessMapper.insert(access);
		} else {
			access.setAccessStatus("ACTIVE");
			access.setSourceType(Func.toStr(body.get("sourceType"), "ADMIN"));
			access.setSourceId(Func.toStr(body.get("sourceId"), ""));
			access.setValidStartAt(toDate(body.get("validStartAt")));
			access.setValidEndAt(toDate(body.get("validEndAt")));
			accessMapper.updateById(access);
		}
		return access;
	}

	@Transactional(rollbackFor = Exception.class)
	public void revokeAccess(Long accessId) {
		TrainingAccessEntity access = accessMapper.selectById(accessId);
		if (access == null || Func.equals(access.getIsDeleted(), 1)) throw new ServiceException("授权记录不存在");
		access.setAccessStatus("REVOKED");
		accessMapper.updateById(access);
	}

	public List<Map<String, Object>> accessList(Long trainingId) {
		List<TrainingAccessEntity> accesses = accessMapper.selectList(Wrappers.<TrainingAccessEntity>lambdaQuery()
			.eq(TrainingAccessEntity::getTrainingId, trainingId)
			.eq(TrainingAccessEntity::getIsDeleted, 0)
			.orderByDesc(TrainingAccessEntity::getCreateTime));
		Map<Long, User> userMap = userService.listByIds(accesses.stream().map(TrainingAccessEntity::getUserId).distinct().collect(Collectors.toList()))
			.stream().collect(Collectors.toMap(User::getId, item -> item, (left, right) -> left));
		return accesses.stream().map(access -> {
			User user = userMap.get(access.getUserId());
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("id", access.getId());
			item.put("userId", access.getUserId());
			item.put("userName", user == null ? "未知用户" : firstNonBlank(user.getRealName(), user.getName(), user.getAccount()));
			item.put("phone", user == null ? "" : maskPhone(user.getPhone()));
			item.put("sourceType", access.getSourceType());
			item.put("sourceId", access.getSourceId());
			item.put("accessStatus", access.getAccessStatus());
			item.put("validStartAt", access.getValidStartAt());
			item.put("validEndAt", access.getValidEndAt());
			item.put("createTime", access.getCreateTime());
			return item;
		}).collect(Collectors.toList());
	}

	private void recalculateCourse(Long trainingId) {
		List<TrainingLessonEntity> lessons = lessonMapper.selectList(Wrappers.<TrainingLessonEntity>lambdaQuery()
			.eq(TrainingLessonEntity::getTrainingId, trainingId)
			.eq(TrainingLessonEntity::getIsDeleted, 0));
		TrainingEntity course = requireCourse(trainingId);
		course.setTotalLessons((int) lessons.stream().filter(item -> Func.equals(item.getStatus(), 1)).count());
		course.setTotalVideoDuration(lessons.stream()
			.filter(item -> Func.equals(item.getStatus(), 1))
			.mapToInt(item -> Func.toInt(item.getDurationSeconds(), 0)).sum());
		trainingService.updateById(course);
	}

	private void markCourseDraft(TrainingEntity course) {
		if (course == null) return;
		if ("PUBLISHED".equalsIgnoreCase(Func.toStr(course.getPublishStatus(), ""))) {
			course.setPublishStatus("DRAFT");
			course.setAuditReason("课程内容已修改，请重新检查并发布");
			trainingService.updateById(course);
		}
	}

	private TrainingEntity requireCourse(Long trainingId) {
		if (trainingId == null) throw new ServiceException("缺少课程ID");
		TrainingEntity course = trainingService.getById(trainingId);
		if (course == null || Func.equals(course.getIsDeleted(), 1)) throw new ServiceException("课程不存在");
		return course;
	}

	private String normalizeMode(String value) {
		String mode = Func.toStr(value, "OFFLINE").trim().toUpperCase(Locale.ROOT);
		return "ONLINE".equals(mode) || "MIXED".equals(mode) ? mode : "OFFLINE";
	}

	private Date toDate(Object value) {
		if (value == null || Func.isBlank(String.valueOf(value))) return null;
		if (value instanceof Date) return (Date) value;
		try {
			return org.springblade.core.tool.utils.DateUtil.parse(String.valueOf(value), "yyyy-MM-dd HH:mm:ss");
		} catch (Exception exception) {
			throw new ServiceException("授权有效时间格式不正确");
		}
	}

	private String maskPhone(String phone) {
		if (Func.isBlank(phone) || phone.length() < 7) return Func.toStr(phone, "");
		return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
	}

	private String firstNonBlank(String... values) {
		for (String value : values) if (Func.isNotBlank(value)) return value;
		return "";
	}
}
