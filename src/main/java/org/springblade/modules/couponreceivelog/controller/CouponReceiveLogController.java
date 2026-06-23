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
package org.springblade.modules.couponreceivelog.controller;

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
import org.springblade.modules.couponreceivelog.pojo.entity.CouponReceiveLogEntity;
import org.springblade.modules.couponreceivelog.pojo.vo.CouponReceiveLogVO;
import org.springblade.modules.couponreceivelog.excel.CouponReceiveLogExcel;
import org.springblade.modules.couponreceivelog.wrapper.CouponReceiveLogWrapper;
import org.springblade.modules.couponreceivelog.service.ICouponReceiveLogService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 领券日志 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-couponreceivelog/couponReceiveLog")
@Tag(name = "领券日志", description = "领券日志接口")
public class CouponReceiveLogController extends BladeController {

	private final ICouponReceiveLogService couponReceiveLogService;

	/**
	 * 领券日志 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入couponReceiveLog")
	public R<CouponReceiveLogVO> detail(CouponReceiveLogEntity couponReceiveLog) {
		CouponReceiveLogEntity detail = couponReceiveLogService.getOne(Condition.getQueryWrapper(couponReceiveLog));
		return R.data(CouponReceiveLogWrapper.build().entityVO(detail));
	}
	/**
	 * 领券日志 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入couponReceiveLog")
	public R<IPage<CouponReceiveLogVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> couponReceiveLog, Query query) {
		IPage<CouponReceiveLogEntity> pages = couponReceiveLogService.page(Condition.getPage(query), Condition.getQueryWrapper(couponReceiveLog, CouponReceiveLogEntity.class));
		return R.data(CouponReceiveLogWrapper.build().pageVO(pages));
	}

	/**
	 * 领券日志 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入couponReceiveLog")
	public R<IPage<CouponReceiveLogVO>> page(CouponReceiveLogVO couponReceiveLog, Query query) {
		IPage<CouponReceiveLogVO> pages = couponReceiveLogService.selectCouponReceiveLogPage(Condition.getPage(query), couponReceiveLog);
		return R.data(pages);
	}

	/**
	 * 领券日志 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入couponReceiveLog")
	public R save(@Valid @RequestBody CouponReceiveLogEntity couponReceiveLog) {
		return R.status(couponReceiveLogService.save(couponReceiveLog));
	}

	/**
	 * 领券日志 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入couponReceiveLog")
	public R update(@Valid @RequestBody CouponReceiveLogEntity couponReceiveLog) {
		return R.status(couponReceiveLogService.updateById(couponReceiveLog));
	}

	/**
	 * 领券日志 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入couponReceiveLog")
	public R submit(@Valid @RequestBody CouponReceiveLogEntity couponReceiveLog) {
		return R.status(couponReceiveLogService.saveOrUpdate(couponReceiveLog));
	}

	/**
	 * 领券日志 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(couponReceiveLogService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-couponReceiveLog")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入couponReceiveLog")
	public void exportCouponReceiveLog(@Parameter(hidden = true) @RequestParam Map<String, Object> couponReceiveLog, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<CouponReceiveLogEntity> queryWrapper = Condition.getQueryWrapper(couponReceiveLog, CouponReceiveLogEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(CouponReceiveLog::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(CouponReceiveLogEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<CouponReceiveLogExcel> list = couponReceiveLogService.exportCouponReceiveLog(queryWrapper);
		ExcelUtil.export(response, "领券日志数据" + DateUtil.time(), "领券日志数据表", list, CouponReceiveLogExcel.class);
	}

}


