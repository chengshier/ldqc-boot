package org.springblade.modules.venueapply.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.venue.pojo.entity.VenueEntity;
import org.springblade.modules.venueapply.pojo.entity.VenueApplyEntity;
import org.springblade.modules.venueapply.service.VenueOnboardingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 场馆入驻申请、审核和场馆运营工作台接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/blade-venue/venue-apply")
@Tag(name = "场馆入驻", description = "用户申请、管理员审核和场馆运营者资料维护")
public class VenueApplyController {

	private final VenueOnboardingService onboardingService;

	@PostMapping("/mobile/submit")
	@Operation(summary = "提交场馆入驻申请")
	public R<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
		return R.data(onboardingService.submit(body, AuthUtil.getUserId()));
	}

	@GetMapping("/mobile/page")
	@Operation(summary = "我的场馆入驻申请")
	public R<IPage<Map<String, Object>>> myPage(@RequestParam(defaultValue = "1") long current,
		@RequestParam(defaultValue = "20") long size) {
		return R.data(onboardingService.myPage(current, size, AuthUtil.getUserId()));
	}

	@GetMapping("/mobile/detail")
	@Operation(summary = "我的场馆入驻申请详情")
	public R<Map<String, Object>> myDetail(@RequestParam Long id) {
		return R.data(onboardingService.myDetail(id, AuthUtil.getUserId()));
	}

	@PostMapping("/mobile/cancel")
	@Operation(summary = "取消待审核入驻申请")
	public R cancel(@RequestBody Map<String, Object> body) {
		Long id = Func.toLong(body.get("id"));
		if (id == null) return R.fail("缺少申请ID");
		onboardingService.cancel(id, AuthUtil.getUserId());
		return R.success("申请已取消");
	}

	@GetMapping("/mobile/my-venues")
	@Operation(summary = "我的运营场馆")
	public R<IPage<VenueEntity>> myVenues(@RequestParam(defaultValue = "1") long current,
		@RequestParam(defaultValue = "20") long size) {
		return R.data(onboardingService.myVenues(current, size, AuthUtil.getUserId()));
	}

	@PostMapping("/mobile/update-venue")
	@Operation(summary = "维护我的场馆资料", description = "只能维护已绑定到当前用户的场馆公开资料")
	public R<VenueEntity> updateVenue(@RequestBody Map<String, Object> body) {
		return R.data(onboardingService.updateMyVenue(body, AuthUtil.getUserId()));
	}

	@IsAdmin
	@GetMapping("/admin/page")
	@Operation(summary = "管理端入驻申请分页")
	public R<IPage<VenueApplyEntity>> adminPage(@RequestParam(defaultValue = "1") long current,
		@RequestParam(defaultValue = "20") long size,
		@RequestParam(required = false) String status,
		@RequestParam(required = false) String keyword) {
		return R.data(onboardingService.adminPage(current, size, status, keyword));
	}

	@IsAdmin
	@GetMapping("/admin/detail")
	@Operation(summary = "管理端入驻申请详情")
	public R<VenueApplyEntity> adminDetail(@RequestParam Long id) {
		return R.data(onboardingService.adminDetail(id));
	}

	@IsAdmin
	@PostMapping("/admin/audit")
	@Operation(summary = "审核场馆入驻申请", description = "action：APPROVE 或 REJECT；驳回必须填写原因")
	public R<VenueApplyEntity> audit(@RequestBody Map<String, Object> body) {
		Long id = Func.toLong(body.get("id"));
		if (id == null) return R.fail("缺少申请ID");
		return R.data(onboardingService.audit(id, Func.toStr(body.get("action"), ""),
			Func.toStr(body.get("reason"), ""), AuthUtil.getUserId()));
	}
}
