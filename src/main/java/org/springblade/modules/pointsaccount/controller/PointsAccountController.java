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
package org.springblade.modules.pointsaccount.controller;

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
import org.springblade.modules.pointsaccount.pojo.entity.PointsAccountEntity;
import org.springblade.modules.pointsaccount.pojo.vo.PointsAccountVO;
import org.springblade.modules.pointsaccount.excel.PointsAccountExcel;
import org.springblade.modules.pointsaccount.wrapper.PointsAccountWrapper;
import org.springblade.modules.pointsaccount.service.IPointsAccountService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 积分账户 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-pointsaccount/pointsAccount")
@Tag(name = "积分账户", description = "积分账户接口")
public class PointsAccountController extends BladeController {

	private final IPointsAccountService pointsAccountService;

	/**
	 * 积分账户 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入pointsAccount")
	public R<PointsAccountVO> detail(PointsAccountEntity pointsAccount) {
		PointsAccountEntity detail = pointsAccountService.getOne(Condition.getQueryWrapper(pointsAccount));
		return R.data(PointsAccountWrapper.build().entityVO(detail));
	}
	/**
	 * 积分账户 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入pointsAccount")
	public R<IPage<PointsAccountVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> pointsAccount, Query query) {
		IPage<PointsAccountEntity> pages = pointsAccountService.page(Condition.getPage(query), Condition.getQueryWrapper(pointsAccount, PointsAccountEntity.class));
		return R.data(PointsAccountWrapper.build().pageVO(pages));
	}

	/**
	 * 积分账户 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入pointsAccount")
	public R<IPage<PointsAccountVO>> page(PointsAccountVO pointsAccount, Query query) {
		IPage<PointsAccountVO> pages = pointsAccountService.selectPointsAccountPage(Condition.getPage(query), pointsAccount);
		return R.data(pages);
	}

	/**
	 * 积分账户 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入pointsAccount")
	public R save(@Valid @RequestBody PointsAccountEntity pointsAccount) {
		return R.status(pointsAccountService.save(pointsAccount));
	}

	/**
	 * 积分账户 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入pointsAccount")
	public R update(@Valid @RequestBody PointsAccountEntity pointsAccount) {
		return R.status(pointsAccountService.updateById(pointsAccount));
	}

	/**
	 * 积分账户 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入pointsAccount")
	public R submit(@Valid @RequestBody PointsAccountEntity pointsAccount) {
		return R.status(pointsAccountService.saveOrUpdate(pointsAccount));
	}

	/**
	 * 积分账户 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(pointsAccountService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-pointsAccount")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入pointsAccount")
	public void exportPointsAccount(@Parameter(hidden = true) @RequestParam Map<String, Object> pointsAccount, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<PointsAccountEntity> queryWrapper = Condition.getQueryWrapper(pointsAccount, PointsAccountEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(PointsAccount::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(PointsAccountEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<PointsAccountExcel> list = pointsAccountService.exportPointsAccount(queryWrapper);
		ExcelUtil.export(response, "积分账户数据" + DateUtil.time(), "积分账户数据表", list, PointsAccountExcel.class);
	}

}


