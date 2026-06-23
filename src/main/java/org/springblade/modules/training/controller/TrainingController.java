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
package org.springblade.modules.training.controller;

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
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.training.pojo.vo.TrainingVO;
import org.springblade.modules.training.excel.TrainingExcel;
import org.springblade.modules.training.wrapper.TrainingWrapper;
import org.springblade.modules.training.service.ITrainingService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 培训课程表 控制器
 *
 * @author BladeX
 * @since 2026-03-10
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-training/training")
@Tag(name = "培训课程表", description = "培训课程表接口")
public class TrainingController extends BladeController {

	private final ITrainingService trainingService;

	/**
	 * 培训课程表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入training")
	public R<TrainingVO> detail(TrainingEntity training) {
		TrainingEntity detail = trainingService.getOne(Condition.getQueryWrapper(training));
		return R.data(TrainingWrapper.build().entityVO(detail));
	}
	/**
	 * 培训课程表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入training")
	public R<IPage<TrainingVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> training, Query query) {
		IPage<TrainingEntity> pages = trainingService.page(Condition.getPage(query), Condition.getQueryWrapper(training, TrainingEntity.class));
		return R.data(TrainingWrapper.build().pageVO(pages));
	}

	/**
	 * 培训课程表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入training")
	public R<IPage<TrainingVO>> page(TrainingVO training, Query query) {
		IPage<TrainingVO> pages = trainingService.selectTrainingPage(Condition.getPage(query), training);
		return R.data(pages);
	}

	/**
	 * 培训课程表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入training")
	public R save(@Valid @RequestBody TrainingEntity training) {
		return R.status(trainingService.save(training));
	}

	/**
	 * 培训课程表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入training")
	public R update(@Valid @RequestBody TrainingEntity training) {
		return R.status(trainingService.updateById(training));
	}

	/**
	 * 培训课程表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入training")
	public R submit(@Valid @RequestBody TrainingEntity training) {
		return R.status(trainingService.saveOrUpdate(training));
	}

	/**
	 * 培训课程表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(trainingService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-training")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入training")
	public void exportTraining(@Parameter(hidden = true) @RequestParam Map<String, Object> training, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<TrainingEntity> queryWrapper = Condition.getQueryWrapper(training, TrainingEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(Training::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(TrainingEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<TrainingExcel> list = trainingService.exportTraining(queryWrapper);
		ExcelUtil.export(response, "培训课程表数据" + DateUtil.time(), "培训课程表数据表", list, TrainingExcel.class);
	}

}
