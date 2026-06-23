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
package org.springblade.modules.pointssigninstat.controller;

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
import org.springblade.modules.pointssigninstat.pojo.entity.PointsSigninStatEntity;
import org.springblade.modules.pointssigninstat.pojo.vo.PointsSigninStatVO;
import org.springblade.modules.pointssigninstat.excel.PointsSigninStatExcel;
import org.springblade.modules.pointssigninstat.wrapper.PointsSigninStatWrapper;
import org.springblade.modules.pointssigninstat.service.IPointsSigninStatService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 签到统计 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-pointssigninstat/pointsSigninStat")
@Tag(name = "签到统计", description = "签到统计接口")
public class PointsSigninStatController extends BladeController {

	private final IPointsSigninStatService pointsSigninStatService;

	/**
	 * 签到统计 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入pointsSigninStat")
	public R<PointsSigninStatVO> detail(PointsSigninStatEntity pointsSigninStat) {
		PointsSigninStatEntity detail = pointsSigninStatService.getOne(Condition.getQueryWrapper(pointsSigninStat));
		return R.data(PointsSigninStatWrapper.build().entityVO(detail));
	}
	/**
	 * 签到统计 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入pointsSigninStat")
	public R<IPage<PointsSigninStatVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> pointsSigninStat, Query query) {
		IPage<PointsSigninStatEntity> pages = pointsSigninStatService.page(Condition.getPage(query), Condition.getQueryWrapper(pointsSigninStat, PointsSigninStatEntity.class));
		return R.data(PointsSigninStatWrapper.build().pageVO(pages));
	}

	/**
	 * 签到统计 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入pointsSigninStat")
	public R<IPage<PointsSigninStatVO>> page(PointsSigninStatVO pointsSigninStat, Query query) {
		IPage<PointsSigninStatVO> pages = pointsSigninStatService.selectPointsSigninStatPage(Condition.getPage(query), pointsSigninStat);
		return R.data(pages);
	}

	/**
	 * 签到统计 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入pointsSigninStat")
	public R save(@Valid @RequestBody PointsSigninStatEntity pointsSigninStat) {
		return R.status(pointsSigninStatService.save(pointsSigninStat));
	}

	/**
	 * 签到统计 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入pointsSigninStat")
	public R update(@Valid @RequestBody PointsSigninStatEntity pointsSigninStat) {
		return R.status(pointsSigninStatService.updateById(pointsSigninStat));
	}

	/**
	 * 签到统计 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入pointsSigninStat")
	public R submit(@Valid @RequestBody PointsSigninStatEntity pointsSigninStat) {
		return R.status(pointsSigninStatService.saveOrUpdate(pointsSigninStat));
	}

	/**
	 * 签到统计 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(pointsSigninStatService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-pointsSigninStat")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入pointsSigninStat")
	public void exportPointsSigninStat(@Parameter(hidden = true) @RequestParam Map<String, Object> pointsSigninStat, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<PointsSigninStatEntity> queryWrapper = Condition.getQueryWrapper(pointsSigninStat, PointsSigninStatEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(PointsSigninStat::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(PointsSigninStatEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<PointsSigninStatExcel> list = pointsSigninStatService.exportPointsSigninStat(queryWrapper);
		ExcelUtil.export(response, "签到统计数据" + DateUtil.time(), "签到统计数据表", list, PointsSigninStatExcel.class);
	}

}


