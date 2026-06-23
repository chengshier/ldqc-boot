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
package org.springblade.modules.outdoor.controller;

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
import org.springblade.modules.outdoor.pojo.entity.OutdoorEntity;
import org.springblade.modules.outdoor.pojo.vo.OutdoorVO;
import org.springblade.modules.outdoor.excel.OutdoorExcel;
import org.springblade.modules.outdoor.wrapper.OutdoorWrapper;
import org.springblade.modules.outdoor.service.IOutdoorService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 户外活动表 控制器
 *
 * @author BladeX
 * @since 2026-03-10
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-outdoor/outdoor")
@Tag(name = "户外活动表", description = "户外活动表接口")
public class OutdoorController extends BladeController {

	private final IOutdoorService outdoorService;

	/**
	 * 户外活动表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入outdoor")
	public R<OutdoorVO> detail(OutdoorEntity outdoor) {
		OutdoorEntity detail = outdoorService.getOne(Condition.getQueryWrapper(outdoor));
		return R.data(OutdoorWrapper.build().entityVO(detail));
	}
	/**
	 * 户外活动表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入outdoor")
	public R<IPage<OutdoorVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> outdoor, Query query) {
		IPage<OutdoorEntity> pages = outdoorService.page(Condition.getPage(query), Condition.getQueryWrapper(outdoor, OutdoorEntity.class));
		return R.data(OutdoorWrapper.build().pageVO(pages));
	}

	/**
	 * 户外活动表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入outdoor")
	public R<IPage<OutdoorVO>> page(OutdoorVO outdoor, Query query) {
		IPage<OutdoorVO> pages = outdoorService.selectOutdoorPage(Condition.getPage(query), outdoor);
		return R.data(pages);
	}

	/**
	 * 户外活动表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入outdoor")
	public R save(@Valid @RequestBody OutdoorEntity outdoor) {
		return R.status(outdoorService.save(outdoor));
	}

	/**
	 * 户外活动表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入outdoor")
	public R update(@Valid @RequestBody OutdoorEntity outdoor) {
		return R.status(outdoorService.updateById(outdoor));
	}

	/**
	 * 户外活动表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入outdoor")
	public R submit(@Valid @RequestBody OutdoorEntity outdoor) {
		return R.status(outdoorService.saveOrUpdate(outdoor));
	}

	/**
	 * 户外活动表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(outdoorService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-outdoor")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入outdoor")
	public void exportOutdoor(@Parameter(hidden = true) @RequestParam Map<String, Object> outdoor, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<OutdoorEntity> queryWrapper = Condition.getQueryWrapper(outdoor, OutdoorEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(Outdoor::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(OutdoorEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<OutdoorExcel> list = outdoorService.exportOutdoor(queryWrapper);
		ExcelUtil.export(response, "户外活动表数据" + DateUtil.time(), "户外活动表数据表", list, OutdoorExcel.class);
	}

}
