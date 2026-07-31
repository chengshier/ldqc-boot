package org.springblade.modules.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.trainingchapter.mapper.TrainingChapterMapper;
import org.springblade.modules.trainingchapter.pojo.entity.TrainingChapterEntity;
import org.springblade.modules.traininglesson.mapper.TrainingLessonMapper;
import org.springblade.modules.traininglesson.pojo.entity.TrainingLessonEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 达人本人课程工作台。
 *
 * <p>所有读写都以后端登录用户为准，不接受客户端传入的达人用户ID。达人可以维护自己的
 * 课程、章节和课时并提交审核，但不能直接发布课程或给其他用户发放播放权限。</p>
 */
@Service
@RequiredArgsConstructor
public class TalentCourseWorkbenchService {

	private final IUserService userService;
	private final ITrainingService trainingService;
	private final TrainingCourseSettingsService settingsService;
	private final TrainingCourseAdminService adminService;
	private final TrainingLessonMediaProcessor mediaProcessor;
	private final TrainingChapterMapper chapterMapper;
	private final TrainingLessonMapper lessonMapper;

	public Map<String, Object> summary(Long userId) {
		requireTalent(userId);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("total", countByStatus(userId, null));
		result.put("draft", countByStatus(userId, "DRAFT"));
		result.put("pending", countByStatus(userId, "PENDING"));
		result.put("published", countByStatus(userId, "PUBLISHED"));
		result.put("rejected", countByStatus(userId, "REJECTED"));
		result.put("offline", countByStatus(userId, "OFFLINE"));
		return result;
	}

	public IPage<TrainingEntity> myCourses(long current, long size, String publishStatus, Long userId) {
		requireTalent(userId);
		String normalizedStatus = normalizePublishStatus(publishStatus, false);
		return trainingService.page(new Page<>(Math.max(current, 1), Math.min(Math.max(size, 1), 50)),
			Wrappers.<TrainingEntity>lambdaQuery()
				.eq(TrainingEntity::getTalentUserId, userId)
				.eq(TrainingEntity::getIsDeleted, 0)
				.eq(Func.isNotBlank(normalizedStatus), TrainingEntity::getPublishStatus, normalizedStatus)
				.orderByDesc(TrainingEntity::getUpdateTime)
				.orderByDesc(TrainingEntity::getCreateTime));
	}

	public Map<String, Object> outline(Long trainingId, Long userId) {
		requireOwnedCourse(trainingId, userId);
		return adminService.outline(trainingId);
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingEntity saveSettings(Map<String, Object> body, Long userId) {
		requireTalent(userId);
		if (body == null) throw new ServiceException("课程参数不能为空");
		Long trainingId = Func.toLong(body.get("id"));
		if (trainingId != null) requireOwnedCourse(trainingId, userId);

		Map<String, Object> safeBody = new LinkedHashMap<>(body);
		safeBody.put("talentUserId", userId);
		safeBody.put("status", 1);
		safeBody.remove("publishStatus");
		safeBody.remove("auditReason");
		return settingsService.save(safeBody);
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingChapterEntity saveChapter(Map<String, Object> body, Long userId) {
		Long trainingId = body == null ? null : Func.toLong(body.get("trainingId"));
		requireOwnedCourse(trainingId, userId);
		return adminService.saveChapter(body);
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteChapter(Long chapterId, Long userId) {
		TrainingChapterEntity chapter = requireOwnedChapter(chapterId, userId);
		adminService.deleteChapter(chapter.getId());
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingLessonEntity saveLesson(Map<String, Object> body, Long userId) {
		Long trainingId = body == null ? null : Func.toLong(body.get("trainingId"));
		requireOwnedCourse(trainingId, userId);
		TrainingLessonEntity lesson = adminService.saveLesson(body);
		if ("VIDEO".equalsIgnoreCase(lesson.getLessonType())
			&& "PROCESSING".equalsIgnoreCase(Func.toStr(lesson.getMediaProcessStatus(), ""))) {
			mediaProcessor.processAsync(lesson.getId());
		}
		return lesson;
	}

	public void reprocessLesson(Long lessonId, Long userId) {
		requireOwnedLesson(lessonId, userId);
		mediaProcessor.processAsync(lessonId);
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteLesson(Long lessonId, Long userId) {
		requireOwnedLesson(lessonId, userId);
		adminService.deleteLesson(lessonId);
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingEntity submitReview(Long trainingId, Long userId) {
		TrainingEntity course = requireOwnedCourse(trainingId, userId);
		String currentStatus = normalizePublishStatus(course.getPublishStatus(), true);
		if ("PENDING".equals(currentStatus)) throw new ServiceException("课程已在审核中，请勿重复提交");
		if ("PUBLISHED".equals(currentStatus)) throw new ServiceException("已发布课程无需重复提交审核");
		if (Func.isBlank(course.getTitle())) throw new ServiceException("请先填写课程标题");
		if (Func.isBlank(course.getCoverImage())) throw new ServiceException("请先上传课程封面");
		if (Func.isBlank(course.getDescription())) throw new ServiceException("请先填写课程介绍");

		String mode = Func.toStr(course.getContentMode(), "OFFLINE").trim().toUpperCase(Locale.ROOT);
		if ("ONLINE".equals(mode) || "MIXED".equals(mode)) validateOnlineCourse(trainingId);
		if (!"ONLINE".equals(mode) && Func.isBlank(course.getLocation()) && Func.isBlank(course.getAddress())) {
			throw new ServiceException("线下或混合课程必须填写上课地点");
		}

		course.setPublishStatus("PENDING");
		course.setAuditReason("达人已提交审核，等待平台复核");
		course.setStatus(1);
		trainingService.updateById(course);
		return course;
	}

	@Transactional(rollbackFor = Exception.class)
	public void offline(Long trainingId, String reason, Long userId) {
		TrainingEntity course = requireOwnedCourse(trainingId, userId);
		if (!"PUBLISHED".equalsIgnoreCase(Func.toStr(course.getPublishStatus(), ""))) {
			throw new ServiceException("只有已发布课程可以申请下架");
		}
		adminService.offlineCourse(trainingId, Func.isBlank(reason) ? "达人主动下架" : reason.trim());
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteDraft(Long trainingId, Long userId) {
		TrainingEntity course = requireOwnedCourse(trainingId, userId);
		String status = normalizePublishStatus(course.getPublishStatus(), true);
		if ("PENDING".equals(status) || "PUBLISHED".equals(status)) {
			throw new ServiceException("审核中或已发布课程不能删除，请先下架或等待审核完成");
		}
		long lessonCount = lessonMapper.selectCount(Wrappers.<TrainingLessonEntity>lambdaQuery()
			.eq(TrainingLessonEntity::getTrainingId, trainingId)
			.eq(TrainingLessonEntity::getIsDeleted, 0));
		if (lessonCount > 0) throw new ServiceException("课程下仍有课时，请先删除课时");
		long chapterCount = chapterMapper.selectCount(Wrappers.<TrainingChapterEntity>lambdaQuery()
			.eq(TrainingChapterEntity::getTrainingId, trainingId)
			.eq(TrainingChapterEntity::getIsDeleted, 0));
		if (chapterCount > 0) throw new ServiceException("课程下仍有章节，请先删除章节");
		course.setIsDeleted(1);
		trainingService.updateById(course);
	}

	private void validateOnlineCourse(Long trainingId) {
		long enabledLessons = lessonMapper.selectCount(Wrappers.<TrainingLessonEntity>lambdaQuery()
			.eq(TrainingLessonEntity::getTrainingId, trainingId)
			.eq(TrainingLessonEntity::getStatus, 1)
			.eq(TrainingLessonEntity::getIsDeleted, 0));
		if (enabledLessons <= 0) throw new ServiceException("线上课程至少需要一个已启用课时");
		for (TrainingLessonEntity lesson : lessonMapper.selectList(Wrappers.<TrainingLessonEntity>lambdaQuery()
			.eq(TrainingLessonEntity::getTrainingId, trainingId)
			.eq(TrainingLessonEntity::getStatus, 1)
			.eq(TrainingLessonEntity::getIsDeleted, 0))) {
			if ("VIDEO".equalsIgnoreCase(lesson.getLessonType())) {
				if (Func.isBlank(lesson.getVideoUrl())) throw new ServiceException("课时“" + lesson.getTitle() + "”尚未上传视频");
				if (!"READY".equalsIgnoreCase(Func.toStr(lesson.getMediaProcessStatus(), ""))) {
					throw new ServiceException("课时“" + lesson.getTitle() + "”的视频尚未处理完成");
				}
			}
		}
	}

	private TrainingEntity requireOwnedCourse(Long trainingId, Long userId) {
		requireTalent(userId);
		if (trainingId == null || trainingId <= 0) throw new ServiceException("缺少课程ID");
		TrainingEntity course = trainingService.getOne(Wrappers.<TrainingEntity>lambdaQuery()
			.eq(TrainingEntity::getId, trainingId)
			.eq(TrainingEntity::getTalentUserId, userId)
			.eq(TrainingEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (course == null) throw new ServiceException("课程不存在或不属于当前达人");
		return course;
	}

	private TrainingChapterEntity requireOwnedChapter(Long chapterId, Long userId) {
		if (chapterId == null || chapterId <= 0) throw new ServiceException("缺少章节ID");
		TrainingChapterEntity chapter = chapterMapper.selectById(chapterId);
		if (chapter == null || Func.equals(chapter.getIsDeleted(), 1)) throw new ServiceException("章节不存在");
		requireOwnedCourse(chapter.getTrainingId(), userId);
		return chapter;
	}

	private TrainingLessonEntity requireOwnedLesson(Long lessonId, Long userId) {
		if (lessonId == null || lessonId <= 0) throw new ServiceException("缺少课时ID");
		TrainingLessonEntity lesson = lessonMapper.selectById(lessonId);
		if (lesson == null || Func.equals(lesson.getIsDeleted(), 1)) throw new ServiceException("课时不存在");
		requireOwnedCourse(lesson.getTrainingId(), userId);
		return lesson;
	}

	private User requireTalent(Long userId) {
		if (userId == null || userId <= 0) throw new ServiceException("请先登录");
		User user = userService.getById(userId);
		if (user == null || Func.equals(user.getIsDeleted(), 1) || !Func.equals(user.getStatus(), 1)) {
			throw new ServiceException("用户不存在或已停用");
		}
		if (!Func.equals(user.getIsTalent(), 1) || !Func.equals(user.getAuthStatus(), 2)) {
			throw new ServiceException("只有已通过认证的达人可以使用课程工作台");
		}
		return user;
	}

	private long countByStatus(Long userId, String status) {
		return trainingService.count(Wrappers.<TrainingEntity>lambdaQuery()
			.eq(TrainingEntity::getTalentUserId, userId)
			.eq(TrainingEntity::getIsDeleted, 0)
			.eq(Func.isNotBlank(status), TrainingEntity::getPublishStatus, status));
	}

	private String normalizePublishStatus(String value, boolean defaultDraft) {
		if (Func.isBlank(value)) return defaultDraft ? "DRAFT" : "";
		String status = value.trim().toUpperCase(Locale.ROOT);
		if (!Objects.equals(status, "DRAFT") && !Objects.equals(status, "PENDING")
			&& !Objects.equals(status, "PUBLISHED") && !Objects.equals(status, "REJECTED")
			&& !Objects.equals(status, "OFFLINE")) {
			throw new ServiceException("课程发布状态不正确");
		}
		return status;
	}
}
