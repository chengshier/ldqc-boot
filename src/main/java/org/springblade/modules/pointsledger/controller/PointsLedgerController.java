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
package org.springblade.modules.pointsledger.controller;

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
import org.springblade.modules.pointsledger.pojo.entity.PointsLedgerEntity;
import org.springblade.modules.pointsledger.pojo.vo.PointsLedgerVO;
import org.springblade.modules.pointsledger.excel.PointsLedgerExcel;
import org.springblade.modules.pointsledger.wrapper.PointsLedgerWrapper;
import org.springblade.modules.pointsledger.service.IPointsLedgerService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 积分流水 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-pointsledger/pointsLedger")
@Tag(name = "积分流水", description = "积分流水接口")
public class PointsLedgerController extends BladeController {

	private final IPointsLedgerService pointsLedgerService;

	/**
	 * 积分流水 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入pointsLedger")
	public R<PointsLedgerVO> detail(PointsLedgerEntity pointsLedger) {
		PointsLedgerEntity detail = pointsLedgerService.getOne(Condition.getQueryWrapper(pointsLedger));
		return R.data(PointsLedgerWrapper.build().entityVO(detail));
	}
	/**
	 * 积分流水 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入pointsLedger")
	public R<IPage<PointsLedgerVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> pointsLedger, Query query) {
		IPage<PointsLedgerEntity> pages = pointsLedgerService.page(Condition.getPage(query), Condition.getQueryWrapper(pointsLedger, PointsLedgerEntity.class));
		return R.data(PointsLedgerWrapper.build().pageVO(pages));
	}

	/**
	 * 积分流水 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入pointsLedger")
	public R<IPage<PointsLedgerVO>> page(PointsLedgerVO pointsLedger, Query query) {
		IPage<PointsLedgerVO> pages = pointsLedgerService.selectPointsLedgerPage(Condition.getPage(query), pointsLedger);
		return R.data(pages);
	}

	/**
	 * 积分流水 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入pointsLedger")
	public R save(@Valid @RequestBody PointsLedgerEntity pointsLedger) {
		return R.status(pointsLedgerService.save(pointsLedger));
	}

	/**
	 * 积分流水 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入pointsLedger")
	public R update(@Valid @RequestBody PointsLedgerEntity pointsLedger) {
		return R.status(pointsLedgerService.updateById(pointsLedger));
	}

	/**
	 * 积分流水 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入pointsLedger")
	public R submit(@Valid @RequestBody PointsLedgerEntity pointsLedger) {
		return R.status(pointsLedgerService.saveOrUpdate(pointsLedger));
	}

	/**
	 * 积分流水 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(pointsLedgerService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-pointsLedger")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入pointsLedger")
	public void exportPointsLedger(@Parameter(hidden = true) @RequestParam Map<String, Object> pointsLedger, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<PointsLedgerEntity> queryWrapper = Condition.getQueryWrapper(pointsLedger, PointsLedgerEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(PointsLedger::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(PointsLedgerEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<PointsLedgerExcel> list = pointsLedgerService.exportPointsLedger(queryWrapper);
		ExcelUtil.export(response, "积分流水数据" + DateUtil.time(), "积分流水数据表", list, PointsLedgerExcel.class);
	}

}


