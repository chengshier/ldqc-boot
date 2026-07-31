package org.springblade.modules.training.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.training.service.TalentCourseWorkbenchService;
import org.springblade.modules.trainingchapter.pojo.entity.TrainingChapterEntity;
import org.springblade.modules.traininglesson.pojo.entity.TrainingLessonEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 已认证达人维护本人课程、章节、课时和审核状态的移动端接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("blade-training/talent-workbench")
@Tag(name = "达人课程工作台", description = "达人本人维护课程并提交平台审核")
public class TalentCourseWorkbenchController {

	private final TalentCourseWorkbenchService workbenchService;

	@GetMapping("/summary")
	@Operation(summary = "达人课程统计")
	public R<Map<String, Object>> summary() {
		return R.data(workbenchService.summary(AuthUtil.getUserId()));
	}

	@GetMapping("/courses")
	@Operation(summary = "我的课程分页")
	public R<IPage<TrainingEntity>> courses(@RequestParam(defaultValue = "1") long current,
		@RequestParam(defaultValue = "10") long size,
		@RequestParam(required = false) String publishStatus) {
		return R.data(workbenchService.myCourses(current, size, publishStatus, AuthUtil.getUserId()));
	}

	@GetMapping("/outline")
	@Operation(summary = "我的课程章节课时总览")
	public R<Map<String, Object>> outline(@RequestParam Long trainingId) {
		return R.data(workbenchService.outline(trainingId, AuthUtil.getUserId()));
	}

	@PostMapping("/settings")
	@Operation(summary = "保存本人课程基础资料")
	public R<TrainingEntity> settings(@RequestBody Map<String, Object> body) {
		return R.data(workbenchService.saveSettings(body, AuthUtil.getUserId()));
	}

	@PostMapping("/chapter/save")
	@Operation(summary = "保存本人课程章节")
	public R<TrainingChapterEntity> saveChapter(@RequestBody Map<String, Object> body) {
		return R.data(workbenchService.saveChapter(body, AuthUtil.getUserId()));
	}

	@PostMapping("/chapter/delete")
	@Operation(summary = "删除本人课程空章节")
	public R<Boolean> deleteChapter(@RequestBody Map<String, Object> body) {
		Long chapterId = Func.toLong(body.get("chapterId"));
		workbenchService.deleteChapter(chapterId, AuthUtil.getUserId());
		return R.data(Boolean.TRUE);
	}

	@PostMapping("/lesson/save")
	@Operation(summary = "保存本人课程课时", description = "视频变化后由服务端启动媒体处理")
	public R<TrainingLessonEntity> saveLesson(@RequestBody Map<String, Object> body) {
		return R.data(workbenchService.saveLesson(body, AuthUtil.getUserId()));
	}

	@PostMapping("/lesson/reprocess")
	@Operation(summary = "重新处理本人课程视频")
	public R<Boolean> reprocessLesson(@RequestBody Map<String, Object> body) {
		Long lessonId = Func.toLong(body.get("lessonId"));
		workbenchService.reprocessLesson(lessonId, AuthUtil.getUserId());
		return R.data(Boolean.TRUE);
	}

	@PostMapping("/lesson/delete")
	@Operation(summary = "删除本人课程课时")
	public R<Boolean> deleteLesson(@RequestBody Map<String, Object> body) {
		Long lessonId = Func.toLong(body.get("lessonId"));
		workbenchService.deleteLesson(lessonId, AuthUtil.getUserId());
		return R.data(Boolean.TRUE);
	}

	@PostMapping("/submit-review")
	@Operation(summary = "提交课程审核", description = "达人不能直接发布，平台审核通过后才会公开展示")
	public R<TrainingEntity> submitReview(@RequestBody Map<String, Object> body) {
		Long trainingId = Func.toLong(body.get("trainingId"));
		return R.data(workbenchService.submitReview(trainingId, AuthUtil.getUserId()));
	}

	@PostMapping("/offline")
	@Operation(summary = "达人主动下架本人课程")
	public R<Boolean> offline(@RequestBody Map<String, Object> body) {
		Long trainingId = Func.toLong(body.get("trainingId"));
		String reason = Func.toStr(body.get("reason"), "达人主动下架");
		workbenchService.offline(trainingId, reason, AuthUtil.getUserId());
		return R.data(Boolean.TRUE);
	}

	@PostMapping("/delete-draft")
	@Operation(summary = "删除本人未发布空课程")
	public R<Boolean> deleteDraft(@RequestBody Map<String, Object> body) {
		Long trainingId = Func.toLong(body.get("trainingId"));
		workbenchService.deleteDraft(trainingId, AuthUtil.getUserId());
		return R.data(Boolean.TRUE);
	}
}
