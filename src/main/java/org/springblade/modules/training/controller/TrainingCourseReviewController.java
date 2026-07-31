package org.springblade.modules.training.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.training.service.TrainingCourseReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 平台课程审核工作台接口。 */
@IsAdmin
@RestController
@RequiredArgsConstructor
@RequestMapping("blade-training/course-review")
@Tag(name = "课程审核", description = "平台审核达人和运营课程")
public class TrainingCourseReviewController {

	private final TrainingCourseReviewService reviewService;

	@GetMapping("/page")
	@Operation(summary = "课程审核分页")
	public R<IPage<TrainingEntity>> page(@RequestParam(defaultValue = "1") long current,
		@RequestParam(defaultValue = "10") long size,
		@RequestParam(required = false) String publishStatus,
		@RequestParam(required = false) String title) {
		return R.data(reviewService.page(current, size, publishStatus, title));
	}

	@GetMapping("/outline")
	@Operation(summary = "审核课程章节课时详情")
	public R<Map<String, Object>> outline(@RequestParam Long trainingId) {
		return R.data(reviewService.outline(trainingId));
	}

	@PostMapping("/approve")
	@Operation(summary = "通过并发布课程")
	public R<TrainingEntity> approve(@RequestBody Map<String, Object> body) {
		return R.data(reviewService.approve(Func.toLong(body.get("trainingId"))));
	}

	@PostMapping("/reject")
	@Operation(summary = "驳回课程")
	public R<TrainingEntity> reject(@RequestBody Map<String, Object> body) {
		Long trainingId = Func.toLong(body.get("trainingId"));
		String reason = Func.toStr(body.get("reason"), "");
		return R.data(reviewService.reject(trainingId, reason));
	}
}
