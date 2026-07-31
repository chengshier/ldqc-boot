package org.springblade.modules.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.training.service.TrainingCourseAdminService;
import org.springblade.modules.training.service.TrainingCourseSettingsService;
import org.springblade.modules.training.service.TrainingLessonMediaProcessor;
import org.springblade.modules.trainingaccess.pojo.entity.TrainingAccessEntity;
import org.springblade.modules.trainingchapter.pojo.entity.TrainingChapterEntity;
import org.springblade.modules.traininglesson.pojo.entity.TrainingLessonEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 课程基础设置、章节、课时、发布和播放授权运营工作台接口。
 */
@IsAdmin
@RestController
@RequiredArgsConstructor
@RequestMapping("blade-training/course-admin")
@Tag(name = "课程运营工作台", description = "运营人员维护课程资料、章节、课时、发布状态和用户播放权限")
public class TrainingCourseAdminController {

	private final TrainingCourseAdminService adminService;
	private final TrainingCourseSettingsService settingsService;
	private final TrainingLessonMediaProcessor mediaProcessor;

	@GetMapping("/outline")
	@Operation(summary = "课程章节课时总览")
	public R<Map<String, Object>> outline(@RequestParam Long trainingId) {
		return R.data(adminService.outline(trainingId));
	}

	@PostMapping("/settings")
	@Operation(summary = "保存课程基础设置", description = "维护课程形态、授权规则、价格、封面和介绍")
	public R<TrainingEntity> settings(@RequestBody Map<String, Object> body) {
		return R.data(settingsService.save(body));
	}

	@PostMapping("/chapter/save")
	@Operation(summary = "新增或修改章节")
	public R<TrainingChapterEntity> saveChapter(@RequestBody Map<String, Object> body) {
		return R.data(adminService.saveChapter(body));
	}

	@PostMapping("/chapter/delete")
	@Operation(summary = "删除空章节")
	public R deleteChapter(@RequestBody Map<String, Object> body) {
		Long chapterId = Func.toLong(body.get("chapterId"));
		if (chapterId == null) return R.fail("缺少章节ID");
		adminService.deleteChapter(chapterId);
		return R.success("章节已删除");
	}

	@PostMapping("/lesson/save")
	@Operation(summary = "新增或修改课时", description = "视频变化后自动进入媒体处理")
	public R<TrainingLessonEntity> saveLesson(@RequestBody Map<String, Object> body) {
		TrainingLessonEntity lesson = adminService.saveLesson(body);
		if ("VIDEO".equalsIgnoreCase(lesson.getLessonType())
			&& "PROCESSING".equalsIgnoreCase(Func.toStr(lesson.getMediaProcessStatus(), ""))) {
			mediaProcessor.processAsync(lesson.getId());
		}
		return R.data(lesson);
	}

	@PostMapping("/lesson/reprocess")
	@Operation(summary = "重新处理课时视频")
	public R reprocessLesson(@RequestBody Map<String, Object> body) {
		Long lessonId = Func.toLong(body.get("lessonId"));
		if (lessonId == null) return R.fail("缺少课时ID");
		mediaProcessor.processAsync(lessonId);
		return R.success("已重新提交媒体处理");
	}

	@PostMapping("/lesson/delete")
	@Operation(summary = "删除课时")
	public R deleteLesson(@RequestBody Map<String, Object> body) {
		Long lessonId = Func.toLong(body.get("lessonId"));
		if (lessonId == null) return R.fail("缺少课时ID");
		adminService.deleteLesson(lessonId);
		return R.success("课时已删除");
	}

	@PostMapping("/publish")
	@Operation(summary = "发布课程", description = "线上课程发布前检查课时和媒体处理状态")
	public R<TrainingEntity> publish(@RequestBody Map<String, Object> body) {
		Long trainingId = Func.toLong(body.get("trainingId"));
		if (trainingId == null) return R.fail("缺少课程ID");
		return R.data(adminService.publishCourse(trainingId));
	}

	@PostMapping("/offline")
	@Operation(summary = "下架课程")
	public R offline(@RequestBody Map<String, Object> body) {
		Long trainingId = Func.toLong(body.get("trainingId"));
		String reason = Func.toStr(body.get("reason"), "");
		if (trainingId == null) return R.fail("缺少课程ID");
		adminService.offlineCourse(trainingId, reason);
		return R.success("课程已下架");
	}

	@GetMapping("/user-options")
	@Operation(summary = "搜索授权用户", description = "按姓名、账号或手机号搜索，不要求运营人员手填用户ID")
	public R<List<Map<String, Object>>> userOptions(@RequestParam String keyword) {
		return R.data(adminService.searchUsers(keyword));
	}

	@GetMapping("/access/list")
	@Operation(summary = "课程授权列表")
	public R<List<Map<String, Object>>> accessList(@RequestParam Long trainingId) {
		return R.data(adminService.accessList(trainingId));
	}

	@PostMapping("/access/grant")
	@Operation(summary = "授予课程播放权限")
	public R<TrainingAccessEntity> grantAccess(@RequestBody Map<String, Object> body) {
		return R.data(adminService.grantAccess(body));
	}

	@PostMapping("/access/revoke")
	@Operation(summary = "撤销课程播放权限")
	public R revokeAccess(@RequestBody Map<String, Object> body) {
		Long accessId = Func.toLong(body.get("accessId"));
		if (accessId == null) return R.fail("缺少授权记录ID");
		adminService.revokeAccess(accessId);
		return R.success("课程权限已撤销");
	}
}
