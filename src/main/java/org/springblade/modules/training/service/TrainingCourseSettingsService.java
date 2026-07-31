package org.springblade.modules.training.service;

import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

/**
 * 课程基础设置服务，供运营课程管理与内容工作台使用。
 */
@Service
@RequiredArgsConstructor
public class TrainingCourseSettingsService {

	private final ITrainingService trainingService;

	@Transactional(rollbackFor = Exception.class)
	public TrainingEntity save(Map<String, Object> body) {
		Long trainingId = Func.toLong(body.get("id"));
		boolean creating = trainingId == null;
		TrainingEntity course = creating ? new TrainingEntity() : trainingService.getById(trainingId);
		if (!creating && (course == null || Func.equals(course.getIsDeleted(), 1))) {
			throw new ServiceException("课程不存在");
		}

		String title = Func.toStr(body.get("title"), "").trim();
		if (Func.isBlank(title)) throw new ServiceException("课程标题不能为空");
		String contentMode = Func.toStr(body.get("contentMode"), "OFFLINE").trim().toUpperCase(Locale.ROOT);
		if (!"OFFLINE".equals(contentMode) && !"ONLINE".equals(contentMode) && !"MIXED".equals(contentMode)) {
			throw new ServiceException("课程形态不正确");
		}

		course.setTitle(title);
		course.setCoverImage(Func.toStr(body.get("coverImage"), "").trim());
		course.setInstructorName(Func.toStr(body.get("instructorName"), "").trim());
		course.setCategory(Func.toStr(body.get("category"), "").trim());
		course.setCourseType(Func.toStr(body.get("courseType"), "").trim());
		course.setDescription(Func.toStr(body.get("description"), "").trim());
		course.setContentMode(contentMode);
		course.setPurchaseRequired(Func.toInt(body.get("purchaseRequired"), 0) == 1 ? 1 : 0);
		course.setPrice(toMoney(body.get("price")));
		course.setDuration(Math.max(0, Func.toInt(body.get("duration"), 0)));
		course.setLocation(Func.toStr(body.get("location"), "").trim());
		course.setAddress(Func.toStr(body.get("address"), "").trim());
		course.setOrgId(Func.toLong(body.get("orgId")));
		course.setTeacherId(Func.toLong(body.get("teacherId")));
		course.setTalentUserId(Func.toLong(body.get("talentUserId")));
		course.setStatus(Func.toInt(body.get("status"), 1) == 1 ? 1 : 0);
		course.setSortOrder(Func.toInt(body.get("sortOrder"), Func.toInt(course.getSortOrder(), 0)));

		if (creating) {
			course.setPublishStatus("DRAFT");
			course.setAuditReason("课程已创建，请完善章节课时并发布");
			course.setTotalLessons(0);
			course.setTotalVideoDuration(0);
			trainingService.save(course);
			return course;
		}

		// 已发布课程修改核心信息后必须重新发布，避免用户看到目录与课程说明不一致。
		if ("PUBLISHED".equalsIgnoreCase(Func.toStr(course.getPublishStatus(), ""))) {
			course.setPublishStatus("DRAFT");
			course.setAuditReason("课程基础信息已修改，请重新检查并发布");
		} else if (Func.isBlank(course.getPublishStatus())) {
			course.setPublishStatus("DRAFT");
		}
		trainingService.updateById(course);
		return course;
	}

	private BigDecimal toMoney(Object value) {
		if (value == null || Func.isBlank(String.valueOf(value))) return BigDecimal.ZERO;
		try {
			BigDecimal amount = new BigDecimal(String.valueOf(value));
			if (amount.compareTo(BigDecimal.ZERO) < 0) throw new ServiceException("课程价格不能小于0");
			return amount.setScale(2, java.math.RoundingMode.HALF_UP);
		} catch (NumberFormatException exception) {
			throw new ServiceException("课程价格格式不正确");
		}
	}
}
