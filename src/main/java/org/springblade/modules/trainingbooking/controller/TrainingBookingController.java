package org.springblade.modules.trainingbooking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.trainingbooking.pojo.entity.TrainingBookingEntity;
import org.springblade.modules.trainingbooking.service.TrainingBookingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 体育课程线下预约接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("blade-training/booking")
@Tag(name = "体育课程预约", description = "线下和混合课程预约申请、确认和完成")
public class TrainingBookingController {

	private final TrainingBookingService bookingService;

	@PostMapping("/submit")
	@Operation(summary = "提交课程预约", description = "用户身份取服务端登录态；本接口不伪造支付状态")
	public R<TrainingBookingEntity> submit(@RequestBody Map<String, Object> body) {
		return R.data(bookingService.submit(body, AuthUtil.getUserId()));
	}

	@GetMapping("/my-page")
	@Operation(summary = "我的课程预约")
	public R<IPage<TrainingBookingEntity>> myPage(@RequestParam(defaultValue = "1") long current,
		@RequestParam(defaultValue = "10") long size,
		@RequestParam(required = false) String bookingStatus) {
		return R.data(bookingService.myPage(current, size, bookingStatus, AuthUtil.getUserId()));
	}

	@GetMapping("/detail")
	@Operation(summary = "我的课程预约详情")
	public R<TrainingBookingEntity> detail(@RequestParam Long bookingId) {
		return R.data(bookingService.myDetail(bookingId, AuthUtil.getUserId()));
	}

	@PostMapping("/cancel")
	@Operation(summary = "取消本人课程预约")
	public R<TrainingBookingEntity> cancel(@RequestBody Map<String, Object> body) {
		Long bookingId = Func.toLong(body.get("bookingId"));
		String reason = Func.toStr(body.get("reason"), "用户主动取消");
		return R.data(bookingService.cancel(bookingId, reason, AuthUtil.getUserId()));
	}

	@IsAdmin
	@GetMapping("/admin-page")
	@Operation(summary = "管理端课程预约分页")
	public R<IPage<TrainingBookingEntity>> adminPage(@RequestParam(defaultValue = "1") long current,
		@RequestParam(defaultValue = "10") long size,
		@RequestParam(required = false) String bookingStatus,
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) Long trainingId) {
		return R.data(bookingService.adminPage(current, size, bookingStatus, keyword, trainingId));
	}

	@IsAdmin
	@PostMapping("/admin-confirm")
	@Operation(summary = "确认课程预约")
	public R<TrainingBookingEntity> confirm(@RequestBody Map<String, Object> body) {
		Long bookingId = Func.toLong(body.get("bookingId"));
		String reason = Func.toStr(body.get("reason"), "平台已确认预约，请按约定时间到场");
		return R.data(bookingService.confirm(bookingId, reason));
	}

	@IsAdmin
	@PostMapping("/admin-reject")
	@Operation(summary = "驳回课程预约")
	public R<TrainingBookingEntity> reject(@RequestBody Map<String, Object> body) {
		Long bookingId = Func.toLong(body.get("bookingId"));
		String reason = Func.toStr(body.get("reason"), "");
		return R.data(bookingService.reject(bookingId, reason));
	}

	@IsAdmin
	@PostMapping("/admin-complete")
	@Operation(summary = "完成课程预约")
	public R<TrainingBookingEntity> complete(@RequestBody Map<String, Object> body) {
		Long bookingId = Func.toLong(body.get("bookingId"));
		String reason = Func.toStr(body.get("reason"), "");
		return R.data(bookingService.complete(bookingId, reason));
	}
}
