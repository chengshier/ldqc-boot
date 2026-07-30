package org.springblade.modules.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

/** 平台课程审核服务。 */
@Service
@RequiredArgsConstructor
public class TrainingCourseReviewService {

	private final ITrainingService trainingService;
	private final TrainingCourseAdminService adminService;

	public IPage<TrainingEntity> page(long current, long size, String publishStatus, String title) {
		String status = normalizeStatus(publishStatus);
		return trainingService.page(new Page<>(Math.max(current, 1), Math.min(Math.max(size, 1), 50)),
			Wrappers.<TrainingEntity>lambdaQuery()
				.eq(TrainingEntity::getIsDeleted, 0)
				.eq(Func.isNotBlank(status), TrainingEntity::getPublishStatus, status)
				.like(Func.isNotBlank(title), TrainingEntity::getTitle, title == null ? "" : title.trim())
				.orderByDesc(TrainingEntity::getUpdateTime)
				.orderByDesc(TrainingEntity::getCreateTime));
	}

	public Map<String, Object> outline(Long trainingId) {
		return adminService.outline(trainingId);
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingEntity approve(Long trainingId) {
		TrainingEntity course = requireCourse(trainingId);
		if (!"PENDING".equalsIgnoreCase(Func.toStr(course.getPublishStatus(), ""))) {
			throw new ServiceException("只有待审核课程可以通过审核");
		}
		return adminService.publishCourse(trainingId);
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingEntity reject(Long trainingId, String reason) {
		TrainingEntity course = requireCourse(trainingId);
		if (!"PENDING".equalsIgnoreCase(Func.toStr(course.getPublishStatus(), ""))) {
			throw new ServiceException("只有待审核课程可以驳回");
		}
		if (Func.isBlank(reason)) throw new ServiceException("驳回时必须填写原因");
		course.setPublishStatus("REJECTED");
		course.setAuditReason(reason.trim());
		course.setStatus(1);
		trainingService.updateById(course);
		return course;
	}

	private TrainingEntity requireCourse(Long trainingId) {
		if (trainingId == null || trainingId <= 0) throw new ServiceException("缺少课程ID");
		TrainingEntity course = trainingService.getOne(Wrappers.<TrainingEntity>lambdaQuery()
			.eq(TrainingEntity::getId, trainingId)
			.eq(TrainingEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (course == null) throw new ServiceException("课程不存在");
		return course;
	}

	private String normalizeStatus(String value) {
		if (Func.isBlank(value)) return "PENDING";
		String status = value.trim().toUpperCase(Locale.ROOT);
		if (!"DRAFT".equals(status) && !"PENDING".equals(status) && !"PUBLISHED".equals(status)
			&& !"REJECTED".equals(status) && !"OFFLINE".equals(status)) {
			throw new ServiceException("课程发布状态不正确");
		}
		return status;
	}
}
