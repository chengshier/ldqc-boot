/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 * <p>
 * Use of this software is governed by the Commercial License Agreement
 * obtained after purchasing a license from BladeX.
 * <p>
 * 1. This software is for development use only under a valid license
 * from BladeX.
 * <p>
 * 2. Redistribution of this software's source code to any third party
 * without a commercial license is strictly prohibited.
 * <p>
 * 3. Licensees may copyright their own code but cannot use segments
 * from this software for such purposes. Copyright of this software
 * remains with BladeX.
 * <p>
 * Using this software signifies agreement to this License, and the software
 * must not be used for illegal purposes.
 * <p>
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY. The author is
 * not liable for any claims arising from secondary or illegal development.
 * <p>
 * Author: Chill Zhuang (bladejava@qq.com)
 */
package org.springblade.modules.usercoupon.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import lombok.AllArgsConstructor;
import jakarta.validation.Valid;

import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.secure.utils.AuthUtil;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.usercoupon.pojo.entity.UserCouponEntity;
import org.springblade.modules.usercoupon.pojo.vo.UserCouponVO;
import org.springblade.modules.usercoupon.excel.UserCouponExcel;
import org.springblade.modules.usercoupon.wrapper.UserCouponWrapper;
import org.springblade.modules.usercoupon.service.IUserCouponService;
import org.springblade.modules.coupontemplate.pojo.entity.CouponTemplateEntity;
import org.springblade.modules.coupontemplate.service.ICouponTemplateService;
import org.springblade.modules.couponverifylog.pojo.entity.CouponVerifyLogEntity;
import org.springblade.modules.couponverifylog.service.ICouponVerifyLogService;
import org.springblade.common.utils.RedisUtils;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户优惠券 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-usercoupon/userCoupon")
@Tag(name = "用户优惠券", description = "用户优惠券接口")
public class UserCouponController extends BladeController {

	private final IUserCouponService userCouponService;
	private final ICouponTemplateService couponTemplateService;
	private final ICouponVerifyLogService couponVerifyLogService;
	private final RedisUtils redisUtils;
	private final JdbcTemplate jdbcTemplate;

	/**
	 * 用户优惠券 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入userCoupon")
	public R<UserCouponVO> detail(UserCouponEntity userCoupon) {
		UserCouponEntity detail = userCouponService.getById(userCoupon.getId());
		if (detail == null || !AuthUtil.getUserId().equals(detail.getUserId())) return R.fail("无权查看该优惠券");
		return R.data(toUserCouponVO(detail));
	}
	/**
	 * 用户优惠券 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入userCoupon")
	public R<IPage<UserCouponVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> userCoupon, Query query) {
		userCoupon.put("userId", AuthUtil.getUserId());
		IPage<UserCouponEntity> pages = userCouponService.page(Condition.getPage(query), Condition.getQueryWrapper(userCoupon, UserCouponEntity.class));
		return R.data(pages.convert(this::toUserCouponVO));
	}

	/**
	 * 用户优惠券 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入userCoupon")
	public R<IPage<UserCouponVO>> page(UserCouponVO userCoupon, Query query) {
		IPage<UserCouponVO> pages = userCouponService.selectUserCouponPage(Condition.getPage(query), userCoupon);
		return R.data(pages);
	}

	/**
	 * 用户优惠券 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入userCoupon")
	public R save(@Valid @RequestBody UserCouponEntity userCoupon) {
		return R.status(userCouponService.save(userCoupon));
	}

	/**
	 * 用户优惠券 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入userCoupon")
	public R update(@Valid @RequestBody UserCouponEntity userCoupon) {
		return R.status(userCouponService.updateById(userCoupon));
	}

	/**
	 * 用户优惠券 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入userCoupon")
	public R submit(@Valid @RequestBody UserCouponEntity userCoupon) {
		return R.status(userCouponService.saveOrUpdate(userCoupon));
	}

	/**
	 * 用户优惠券 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(userCouponService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-userCoupon")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入userCoupon")
	public void exportUserCoupon(@Parameter(hidden = true) @RequestParam Map<String, Object> userCoupon, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<UserCouponEntity> queryWrapper = Condition.getQueryWrapper(userCoupon, UserCouponEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(UserCoupon::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(UserCouponEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<UserCouponExcel> list = userCouponService.exportUserCoupon(queryWrapper);
		ExcelUtil.export(response, "用户优惠券数据" + DateUtil.time(), "用户优惠券数据表", list, UserCouponExcel.class);
	}

	@PostMapping("/use")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "核销优惠券", description  = "传入couponNo、orderNo")
	public R<String> useCoupon(@RequestParam String couponNo, @RequestParam(required = false) String orderNo) {
		Long merchantUserId = AuthUtil.getUserId();
		String result = userCouponService.useCoupon(couponNo, orderNo, merchantUserId);
		return "核销成功".equals(result) ? R.data(result) : R.fail(result);
	}

	@PostMapping("/release")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "释放锁券", description  = "传入couponNo")
	public R<String> releaseCoupon(@RequestParam String couponNo) {
		String result = userCouponService.releaseCoupon(couponNo);
		return "释放成功".equals(result) ? R.data(result) : R.fail(result);
	}

	@GetMapping("/qrcode-token")
	@Operation(summary = "生成优惠券动态二维码")
	public R<Map<String, Object>> qrCodeToken(@RequestParam Long userCouponId) {
		UserCouponEntity coupon = userCouponService.getById(userCouponId);
		if (coupon == null || !AuthUtil.getUserId().equals(coupon.getUserId())) return R.fail("无权操作该优惠券");
		if (!isUsable(coupon)) return R.fail("当前券不可核销");
		String token = UUID.randomUUID().toString().replace("-", "");
		redisUtils.set("coupon:qr:" + token, String.valueOf(coupon.getId()), 90);
		Map<String, Object> result = new HashMap<>();
		result.put("qrToken", token); result.put("expiresIn", 90); result.put("refreshInSeconds", 60);
		return R.data(result);
	}

	@GetMapping("/verify-permission")
	@Operation(summary = "核销权限")
	public R<Map<String, Object>> verifyPermission() {
		Map<String, Object> result = new HashMap<>();
		result.put("canVerify", hasVerifierScope()); result.put("venueName", hasVerifierScope() ? "已授权核销场馆" : "");
		return R.data(result);
	}

	@PostMapping("/verify-scan")
	@Operation(summary = "扫描优惠券二维码")
	public R<Map<String, Object>> verifyScan(@RequestBody Map<String, String> body) {
		if (!hasVerifierScope()) return R.fail("当前账号暂无核销权限");
		Object value = redisUtils.get("coupon:qr:" + body.get("qrToken"));
		if (value == null) return R.fail("二维码已失效，请让用户刷新后重试");
		UserCouponEntity coupon = userCouponService.getById(Long.valueOf(String.valueOf(value)));
		if (coupon == null || !canVerifyCoupon(coupon)) return R.fail("该优惠券不可在当前场馆核销");
		redisUtils.set("coupon:verify-session:" + AuthUtil.getUserId() + ":" + coupon.getId(), "1", 300);
		CouponTemplateEntity template = couponTemplateService.getById(coupon.getCouponTemplateId());
		Map<String, Object> result = new HashMap<>();
		result.put("userCouponId", coupon.getId()); result.put("couponNo", coupon.getCouponNo());
		result.put("couponTemplateId", coupon.getCouponTemplateId()); result.put("couponName", template == null ? "优惠券" : template.getCouponName());
		result.put("couponType", template == null ? "CASH" : template.getCouponType()); result.put("status", coupon.getCouponStatus());
		result.put("remainDurationMinutes", coupon.getRemainDurationMinutes()); result.put("remainTimes", coupon.getRemainTimes());
		result.put("validStartAt", coupon.getValidStartAt()); result.put("validEndAt", coupon.getValidEndAt());
		result.put("verifyRecords", couponVerifyLogService.list(Condition.getQueryWrapper(new CouponVerifyLogEntity()).eq("user_coupon_id", coupon.getId())));
		return R.data(result);
	}

	@PostMapping("/verify-confirm")
	@Operation(summary = "确认核销优惠券")
	public R<Map<String, Object>> verifyConfirm(@RequestBody Map<String, Object> body) {
		if (!hasVerifierScope()) return R.fail("当前账号暂无核销权限");
		Long couponId = Long.valueOf(String.valueOf(body.get("userCouponId")));
		String sessionKey = "coupon:verify-session:" + AuthUtil.getUserId() + ":" + couponId;
		if (redisUtils.get(sessionKey) == null) return R.fail("请重新扫码后再核销");
		Boolean locked = redisUtils.getRedisTemplate().opsForValue().setIfAbsent("coupon:verify-lock:" + couponId, "1", 30, TimeUnit.SECONDS);
		if (!Boolean.TRUE.equals(locked)) return R.fail("该优惠券正在核销，请勿重复提交");
		try {
			UserCouponEntity coupon = userCouponService.getById(couponId);
			if (coupon == null || !canVerifyCoupon(coupon)) return R.fail("该优惠券不可在当前场馆核销");
			boolean full = "FULL".equalsIgnoreCase(String.valueOf(body.get("verifyMode")));
			String message = userCouponService.useCouponById(couponId, null, AuthUtil.getUserId(), full, toInt(body.get("consumeDurationMinutes")), toInt(body.get("consumeTimes")));
			if (!"核销成功".equals(message)) return R.fail(message);
			redisUtils.delete(sessionKey);
			Map<String, Object> result = new HashMap<>(); result.put("success", true); result.put("message", message); result.put("userCouponId", couponId);
			return R.data(result);
		} finally { redisUtils.delete("coupon:verify-lock:" + couponId); }
	}

	@GetMapping("/verify-records")
	@Operation(summary = "获取我的优惠券核销记录")
	public R<List<CouponVerifyLogEntity>> verifyRecords(@RequestParam Long userCouponId) {
		UserCouponEntity coupon = userCouponService.getById(userCouponId);
		if (coupon == null || !AuthUtil.getUserId().equals(coupon.getUserId())) return R.fail("无权查看核销记录");
		return R.data(couponVerifyLogService.list(Condition.getQueryWrapper(new CouponVerifyLogEntity()).eq("user_coupon_id", userCouponId)));
	}

	private boolean hasVerifierScope() {
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM coupon_verifier_scope WHERE verifier_user_id = ? AND status = 1 AND is_deleted = 0", Integer.class, AuthUtil.getUserId());
		return count != null && count > 0;
	}

	private boolean canVerifyCoupon(UserCouponEntity coupon) {
		if (!isUsable(coupon)) return false;
		CouponTemplateEntity template = couponTemplateService.getById(coupon.getCouponTemplateId());
		if (template == null) return false;
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM coupon_verifier_scope WHERE verifier_user_id = ? AND status = 1 AND is_deleted = 0 AND ((scope_type = 'ALL' AND scope_ref_id = 'ALL') OR (scope_type = ? AND scope_ref_id = ?))", Integer.class, AuthUtil.getUserId(), template.getScopeType(), template.getScopeRefId());
		return count != null && count > 0;
	}

	private boolean isUsable(UserCouponEntity coupon) {
		return ("UNUSED".equalsIgnoreCase(coupon.getCouponStatus()) || "PARTIAL_USED".equalsIgnoreCase(coupon.getCouponStatus())) && (coupon.getValidEndAt() == null || !coupon.getValidEndAt().before(new java.util.Date()));
	}

	private UserCouponVO toUserCouponVO(UserCouponEntity coupon) {
		UserCouponVO vo = UserCouponWrapper.build().entityVO(coupon);
		CouponTemplateEntity template = couponTemplateService.getById(coupon.getCouponTemplateId());
		if (template == null) return vo;
		vo.setCouponName(template.getCouponName());
		vo.setCouponType(template.getCouponType());
		vo.setScopeType(template.getScopeType());
		vo.setScopeRefId(template.getScopeRefId());
		vo.setValidType(template.getValidType());
		vo.setAcquireType(template.getAcquireType());
		vo.setExtJson(template.getExtJson());
		vo.setThresholdAmount(template.getThresholdAmount());
		vo.setDiscountAmount(template.getDiscountAmount());
		vo.setDurationMinutes(template.getDurationMinutes());
		vo.setTotalTimes(template.getTotalTimes());
		vo.setCostPoints(template.getCostPoints());
		vo.setMinGrowthLevel(template.getMinGrowthLevel());
		vo.setTemplateValidStartAt(template.getValidStartAt());
		vo.setTemplateValidEndAt(template.getValidEndAt());
		vo.setValidDays(template.getValidDays());
		return vo;
	}

	private int toInt(Object value) { try { return value == null ? 0 : Integer.parseInt(String.valueOf(value)); } catch (Exception ignored) { return 0; } }

}


