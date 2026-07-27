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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springblade.modules.usercoupon.pojo.entity.UserCouponEntity;
import org.springblade.modules.usercoupon.pojo.dto.UserCouponVerifyConfirmRequest;
import org.springblade.modules.usercoupon.pojo.vo.UserCouponVO;
import org.springblade.modules.usercoupon.excel.UserCouponExcel;
import org.springblade.modules.usercoupon.service.IUserCouponService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import java.util.Map;
import java.util.List;
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

	/**
	 * 用户优惠券 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入userCoupon")
	public R<UserCouponVO> detail(@RequestParam Long id) {
		return R.data(userCouponService.buildCouponDetail(id));
	}
	/**
	 * 用户优惠券 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入userCoupon")
	public R<IPage<UserCouponVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> userCoupon, Query query) {
		IPage<UserCouponEntity> pages = userCouponService.page(Condition.getPage(query), Condition.getQueryWrapper(userCoupon, UserCouponEntity.class));
		IPage<UserCouponVO> result = new Page<>(pages.getCurrent(), pages.getSize(), pages.getTotal());
		result.setRecords(userCouponService.buildCouponList(pages.getRecords()));
		return R.data(result);
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
	@ApiOperationSupport(order = 12)
	@Operation(summary = "获取动态二维码令牌", description  = "传入userCouponId")
	public R<Map<String, Object>> getQrCodeToken(@RequestParam Long userCouponId) {
		return R.data(userCouponService.getQrCodeToken(userCouponId, AuthUtil.getUserId()));
	}

	@GetMapping("/verify-records")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "获取核销记录", description  = "传入userCouponId")
	public R<List<Map<String, Object>>> getVerifyRecords(@RequestParam Long userCouponId) {
		return R.data(userCouponService.getVerifyRecords(userCouponId));
	}

	@GetMapping("/verify-permission")
	@ApiOperationSupport(order = 14)
	@Operation(summary = "获取核销权限", description  = "当前登录账号的核销权限")
	public R<Map<String, Object>> getVerifyPermission() {
		return R.data(userCouponService.getVerifyPermission(AuthUtil.getUserId()));
	}

	@PostMapping("/verify-scan")
	@ApiOperationSupport(order = 15)
	@Operation(summary = "扫码预检", description  = "传入二维码令牌")
	public R<UserCouponVO> verifyScan(@RequestBody Map<String, String> payload) {
		return R.data(userCouponService.scanVerify(payload.get("qrToken"), AuthUtil.getUserId()));
	}

	@PostMapping("/verify-confirm")
	@ApiOperationSupport(order = 16)
	@Operation(summary = "核销确认", description  = "支持整张核销和部分核销")
	public R<Map<String, Object>> verifyConfirm(@RequestBody UserCouponVerifyConfirmRequest request) {
		return R.data(userCouponService.confirmVerify(request, AuthUtil.getUserId()));
	}

}


