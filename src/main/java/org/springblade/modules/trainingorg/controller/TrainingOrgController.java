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
package org.springblade.modules.trainingorg.controller;

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
import org.springblade.modules.trainingorg.pojo.entity.TrainingOrgEntity;
import org.springblade.modules.trainingorg.pojo.vo.TrainingOrgVO;
import org.springblade.modules.trainingorg.excel.TrainingOrgExcel;
import org.springblade.modules.trainingorg.wrapper.TrainingOrgWrapper;
import org.springblade.modules.trainingorg.service.ITrainingOrgService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 培训机构表 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-trainingorg/trainingOrg")
@Tag(name = "培训机构表", description = "培训机构表接口")
public class TrainingOrgController extends BladeController {

	private final ITrainingOrgService trainingOrgService;

	/**
	 * 培训机构表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入trainingOrg")
	public R<TrainingOrgVO> detail(TrainingOrgEntity trainingOrg) {
		if (trainingOrg == null || trainingOrg.getId() == null || trainingOrg.getId() <= 0) {
			return R.data(null);
		}
		TrainingOrgEntity detail = trainingOrgService.getOne(Condition.getQueryWrapper(trainingOrg));
		if (detail == null) {
			return R.data(null);
		}
		return R.data(TrainingOrgWrapper.build().entityVO(detail));
	}
	/**
	 * 培训机构表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入trainingOrg")
	public R<IPage<TrainingOrgVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> trainingOrg, Query query) {
		IPage<TrainingOrgEntity> pages = trainingOrgService.page(Condition.getPage(query), Condition.getQueryWrapper(trainingOrg, TrainingOrgEntity.class));
		return R.data(TrainingOrgWrapper.build().pageVO(pages));
	}

	@GetMapping("/dicList")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入trainingOrg")
	public R<List<TrainingOrgEntity>> dicList(@Parameter(hidden = true) @RequestParam Map<String, Object> trainingOrg, Query query) {
		return R.data(trainingOrgService.list());
	}

	/**
	 * 培训机构表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入trainingOrg")
	public R<IPage<TrainingOrgVO>> page(TrainingOrgVO trainingOrg, Query query) {
		IPage<TrainingOrgVO> pages = trainingOrgService.selectTrainingOrgPage(Condition.getPage(query), trainingOrg);
		return R.data(pages);
	}

	/**
	 * 培训机构表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入trainingOrg")
	public R save(@Valid @RequestBody TrainingOrgEntity trainingOrg) {
		return R.status(trainingOrgService.save(trainingOrg));
	}

	/**
	 * 培训机构表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入trainingOrg")
	public R update(@Valid @RequestBody TrainingOrgEntity trainingOrg) {
		return R.status(trainingOrgService.updateById(trainingOrg));
	}

	/**
	 * 培训机构表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入trainingOrg")
	public R submit(@Valid @RequestBody TrainingOrgEntity trainingOrg) {
		return R.status(trainingOrgService.saveOrUpdate(trainingOrg));
	}

	/**
	 * 培训机构表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(trainingOrgService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-trainingOrg")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入trainingOrg")
	public void exportTrainingOrg(@Parameter(hidden = true) @RequestParam Map<String, Object> trainingOrg, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<TrainingOrgEntity> queryWrapper = Condition.getQueryWrapper(trainingOrg, TrainingOrgEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(TrainingOrg::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(TrainingOrgEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<TrainingOrgExcel> list = trainingOrgService.exportTrainingOrg(queryWrapper);
		ExcelUtil.export(response, "培训机构表数据" + DateUtil.time(), "培训机构表数据表", list, TrainingOrgExcel.class);
	}

}
