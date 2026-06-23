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
package org.springblade.modules.pointsrule.controller;

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
import org.springblade.modules.pointsrule.pojo.entity.PointsRuleEntity;
import org.springblade.modules.pointsrule.pojo.vo.PointsRuleVO;
import org.springblade.modules.pointsrule.excel.PointsRuleExcel;
import org.springblade.modules.pointsrule.wrapper.PointsRuleWrapper;
import org.springblade.modules.pointsrule.service.IPointsRuleService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 积分规则 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-pointsrule/pointsRule")
@Tag(name = "积分规则", description = "积分规则接口")
public class PointsRuleController extends BladeController {

	private final IPointsRuleService pointsRuleService;

	/**
	 * 积分规则 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入pointsRule")
	public R<PointsRuleVO> detail(PointsRuleEntity pointsRule) {
		PointsRuleEntity detail = pointsRuleService.getOne(Condition.getQueryWrapper(pointsRule));
		return R.data(PointsRuleWrapper.build().entityVO(detail));
	}
	/**
	 * 积分规则 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入pointsRule")
	public R<IPage<PointsRuleVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> pointsRule, Query query) {
		IPage<PointsRuleEntity> pages = pointsRuleService.page(Condition.getPage(query), Condition.getQueryWrapper(pointsRule, PointsRuleEntity.class));
		return R.data(PointsRuleWrapper.build().pageVO(pages));
	}

	/**
	 * 积分规则 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入pointsRule")
	public R<IPage<PointsRuleVO>> page(PointsRuleVO pointsRule, Query query) {
		IPage<PointsRuleVO> pages = pointsRuleService.selectPointsRulePage(Condition.getPage(query), pointsRule);
		return R.data(pages);
	}

	/**
	 * 积分规则 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入pointsRule")
	public R save(@Valid @RequestBody PointsRuleEntity pointsRule) {
		return R.status(pointsRuleService.save(pointsRule));
	}

	/**
	 * 积分规则 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入pointsRule")
	public R update(@Valid @RequestBody PointsRuleEntity pointsRule) {
		return R.status(pointsRuleService.updateById(pointsRule));
	}

	/**
	 * 积分规则 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入pointsRule")
	public R submit(@Valid @RequestBody PointsRuleEntity pointsRule) {
		return R.status(pointsRuleService.saveOrUpdate(pointsRule));
	}

	/**
	 * 积分规则 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(pointsRuleService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-pointsRule")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入pointsRule")
	public void exportPointsRule(@Parameter(hidden = true) @RequestParam Map<String, Object> pointsRule, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<PointsRuleEntity> queryWrapper = Condition.getQueryWrapper(pointsRule, PointsRuleEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(PointsRule::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(PointsRuleEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<PointsRuleExcel> list = pointsRuleService.exportPointsRule(queryWrapper);
		ExcelUtil.export(response, "积分规则数据" + DateUtil.time(), "积分规则数据表", list, PointsRuleExcel.class);
	}

	@PostMapping("/sign-in")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "签到领豆", description  = "当前登录用户签到并领取绿豆")
	public R<String> signIn() {
		String result = pointsRuleService.signIn(AuthUtil.getUserId());
		if (result != null && result.startsWith("签到成功")) {
			return R.data(result);
		}
		return R.fail(result);
	}

}


