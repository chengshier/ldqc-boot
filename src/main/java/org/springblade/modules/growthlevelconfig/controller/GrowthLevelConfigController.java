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
package org.springblade.modules.growthlevelconfig.controller;

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
import org.springblade.modules.growthlevelconfig.pojo.entity.GrowthLevelConfigEntity;
import org.springblade.modules.growthlevelconfig.pojo.vo.GrowthLevelConfigVO;
import org.springblade.modules.growthlevelconfig.excel.GrowthLevelConfigExcel;
import org.springblade.modules.growthlevelconfig.wrapper.GrowthLevelConfigWrapper;
import org.springblade.modules.growthlevelconfig.service.IGrowthLevelConfigService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 成长等级配置 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-growthlevelconfig/growthLevelConfig")
@Tag(name = "成长等级配置", description = "成长等级配置接口")
public class GrowthLevelConfigController extends BladeController {

	private final IGrowthLevelConfigService growthLevelConfigService;

	/**
	 * 成长等级配置 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入growthLevelConfig")
	public R<GrowthLevelConfigVO> detail(GrowthLevelConfigEntity growthLevelConfig) {
		GrowthLevelConfigEntity detail = growthLevelConfigService.getOne(Condition.getQueryWrapper(growthLevelConfig));
		return R.data(GrowthLevelConfigWrapper.build().entityVO(detail));
	}
	/**
	 * 成长等级配置 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入growthLevelConfig")
	public R<IPage<GrowthLevelConfigVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> growthLevelConfig, Query query) {
		IPage<GrowthLevelConfigEntity> pages = growthLevelConfigService.page(Condition.getPage(query), Condition.getQueryWrapper(growthLevelConfig, GrowthLevelConfigEntity.class));
		return R.data(GrowthLevelConfigWrapper.build().pageVO(pages));
	}

	/**
	 * 成长等级配置 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入growthLevelConfig")
	public R<IPage<GrowthLevelConfigVO>> page(GrowthLevelConfigVO growthLevelConfig, Query query) {
		IPage<GrowthLevelConfigVO> pages = growthLevelConfigService.selectGrowthLevelConfigPage(Condition.getPage(query), growthLevelConfig);
		return R.data(pages);
	}

	/**
	 * 成长等级配置 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入growthLevelConfig")
	public R save(@Valid @RequestBody GrowthLevelConfigEntity growthLevelConfig) {
		return R.status(growthLevelConfigService.save(growthLevelConfig));
	}

	/**
	 * 成长等级配置 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入growthLevelConfig")
	public R update(@Valid @RequestBody GrowthLevelConfigEntity growthLevelConfig) {
		return R.status(growthLevelConfigService.updateById(growthLevelConfig));
	}

	/**
	 * 成长等级配置 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入growthLevelConfig")
	public R submit(@Valid @RequestBody GrowthLevelConfigEntity growthLevelConfig) {
		return R.status(growthLevelConfigService.saveOrUpdate(growthLevelConfig));
	}

	/**
	 * 成长等级配置 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(growthLevelConfigService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-growthLevelConfig")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入growthLevelConfig")
	public void exportGrowthLevelConfig(@Parameter(hidden = true) @RequestParam Map<String, Object> growthLevelConfig, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<GrowthLevelConfigEntity> queryWrapper = Condition.getQueryWrapper(growthLevelConfig, GrowthLevelConfigEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(GrowthLevelConfig::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(GrowthLevelConfigEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<GrowthLevelConfigExcel> list = growthLevelConfigService.exportGrowthLevelConfig(queryWrapper);
		ExcelUtil.export(response, "成长等级配置数据" + DateUtil.time(), "成长等级配置数据表", list, GrowthLevelConfigExcel.class);
	}

}


