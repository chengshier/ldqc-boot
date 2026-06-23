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
package org.springblade.modules.couponverifylog.controller;

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
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.couponverifylog.pojo.entity.CouponVerifyLogEntity;
import org.springblade.modules.couponverifylog.pojo.vo.CouponVerifyLogVO;
import org.springblade.modules.couponverifylog.excel.CouponVerifyLogExcel;
import org.springblade.modules.couponverifylog.wrapper.CouponVerifyLogWrapper;
import org.springblade.modules.couponverifylog.service.ICouponVerifyLogService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 优惠券核销日志 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-couponverifylog/couponVerifyLog")
@Tag(name = "优惠券核销日志", description = "优惠券核销日志接口")
public class CouponVerifyLogController extends BladeController {

	private final ICouponVerifyLogService couponVerifyLogService;

	/**
	 * 优惠券核销日志 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入couponVerifyLog")
	public R<CouponVerifyLogVO> detail(CouponVerifyLogEntity couponVerifyLog) {
		CouponVerifyLogEntity detail = couponVerifyLogService.getOne(Condition.getQueryWrapper(couponVerifyLog));
		return R.data(CouponVerifyLogWrapper.build().entityVO(detail));
	}
	/**
	 * 优惠券核销日志 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入couponVerifyLog")
	public R<IPage<CouponVerifyLogVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> couponVerifyLog, Query query) {
		IPage<CouponVerifyLogEntity> pages = couponVerifyLogService.page(Condition.getPage(query), Condition.getQueryWrapper(couponVerifyLog, CouponVerifyLogEntity.class));
		return R.data(CouponVerifyLogWrapper.build().pageVO(pages));
	}

	/**
	 * 优惠券核销日志 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入couponVerifyLog")
	public R<IPage<CouponVerifyLogVO>> page(CouponVerifyLogVO couponVerifyLog, Query query) {
		IPage<CouponVerifyLogVO> pages = couponVerifyLogService.selectCouponVerifyLogPage(Condition.getPage(query), couponVerifyLog);
		return R.data(pages);
	}

	/**
	 * 优惠券核销日志 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入couponVerifyLog")
	public R save(@Valid @RequestBody CouponVerifyLogEntity couponVerifyLog) {
		return R.status(couponVerifyLogService.save(couponVerifyLog));
	}

	/**
	 * 优惠券核销日志 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入couponVerifyLog")
	public R update(@Valid @RequestBody CouponVerifyLogEntity couponVerifyLog) {
		return R.status(couponVerifyLogService.updateById(couponVerifyLog));
	}

	/**
	 * 优惠券核销日志 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入couponVerifyLog")
	public R submit(@Valid @RequestBody CouponVerifyLogEntity couponVerifyLog) {
		return R.status(couponVerifyLogService.saveOrUpdate(couponVerifyLog));
	}

	/**
	 * 优惠券核销日志 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(couponVerifyLogService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-couponVerifyLog")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入couponVerifyLog")
	public void exportCouponVerifyLog(@Parameter(hidden = true) @RequestParam Map<String, Object> couponVerifyLog, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<CouponVerifyLogEntity> queryWrapper = Condition.getQueryWrapper(couponVerifyLog, CouponVerifyLogEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(CouponVerifyLog::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(CouponVerifyLogEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<CouponVerifyLogExcel> list = couponVerifyLogService.exportCouponVerifyLog(queryWrapper);
		ExcelUtil.export(response, "优惠券核销日志数据" + DateUtil.time(), "优惠券核销日志数据表", list, CouponVerifyLogExcel.class);
	}

}


