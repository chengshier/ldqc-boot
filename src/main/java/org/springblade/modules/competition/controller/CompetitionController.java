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
package org.springblade.modules.competition.controller;

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
import org.springblade.modules.competition.pojo.entity.CompetitionEntity;
import org.springblade.modules.competition.pojo.vo.CompetitionVO;
import org.springblade.modules.competition.excel.CompetitionExcel;
import org.springblade.modules.competition.wrapper.CompetitionWrapper;
import org.springblade.modules.competition.service.ICompetitionService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 赛事表 控制器
 *
 * @author BladeX
 * @since 2026-03-10
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-competition/competition")
@Tag(name = "赛事表", description = "赛事表接口")
public class CompetitionController extends BladeController {

	private final ICompetitionService competitionService;

	/**
	 * 赛事表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入competition")
	public R<CompetitionVO> detail(CompetitionEntity competition) {
		CompetitionEntity detail = competitionService.getOne(Condition.getQueryWrapper(competition));
		return R.data(CompetitionWrapper.build().entityVO(detail));
	}
	/**
	 * 赛事表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入competition")
	public R<IPage<CompetitionVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> competition, Query query) {
		IPage<CompetitionEntity> pages = competitionService.page(Condition.getPage(query), Condition.getQueryWrapper(competition, CompetitionEntity.class));
		return R.data(CompetitionWrapper.build().pageVO(pages));
	}

	/**
	 * 赛事表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入competition")
	public R<IPage<CompetitionVO>> page(CompetitionVO competition, Query query) {
		IPage<CompetitionVO> pages = competitionService.selectCompetitionPage(Condition.getPage(query), competition);
		return R.data(pages);
	}

	/**
	 * 赛事表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入competition")
	public R save(@Valid @RequestBody CompetitionEntity competition) {
		return R.status(competitionService.save(competition));
	}

	/**
	 * 赛事表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入competition")
	public R update(@Valid @RequestBody CompetitionEntity competition) {
		return R.status(competitionService.updateById(competition));
	}

	/**
	 * 赛事表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入competition")
	public R submit(@Valid @RequestBody CompetitionEntity competition) {
		return R.status(competitionService.saveOrUpdate(competition));
	}

	/**
	 * 赛事表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(competitionService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-competition")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入competition")
	public void exportCompetition(@Parameter(hidden = true) @RequestParam Map<String, Object> competition, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<CompetitionEntity> queryWrapper = Condition.getQueryWrapper(competition, CompetitionEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(Competition::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(CompetitionEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<CompetitionExcel> list = competitionService.exportCompetition(queryWrapper);
		ExcelUtil.export(response, "赛事表数据" + DateUtil.time(), "赛事表数据表", list, CompetitionExcel.class);
	}

}
