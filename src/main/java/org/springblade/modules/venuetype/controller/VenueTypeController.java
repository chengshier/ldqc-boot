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
package org.springblade.modules.venuetype.controller;

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
import org.springblade.modules.venuetype.pojo.entity.VenueTypeEntity;
import org.springblade.modules.venuetype.pojo.vo.VenueTypeVO;
import org.springblade.modules.venuetype.excel.VenueTypeExcel;
import org.springblade.modules.venuetype.wrapper.VenueTypeWrapper;
import org.springblade.modules.venuetype.service.IVenueTypeService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 场馆类型表 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-venuetype/venueType")
@Tag(name = "场馆类型表", description = "场馆类型表接口")
public class VenueTypeController extends BladeController {

	private final IVenueTypeService venueTypeService;

	/**
	 * 场馆类型表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入venueType")
	public R<VenueTypeVO> detail(VenueTypeEntity venueType) {
		VenueTypeEntity detail = venueTypeService.getOne(Condition.getQueryWrapper(venueType));
		return R.data(VenueTypeWrapper.build().entityVO(detail));
	}
	/**
	 * 场馆类型表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入venueType")
	public R<IPage<VenueTypeVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> venueType, Query query) {
		IPage<VenueTypeEntity> pages = venueTypeService.page(Condition.getPage(query), Condition.getQueryWrapper(venueType, VenueTypeEntity.class));
		return R.data(VenueTypeWrapper.build().pageVO(pages));
	}

	/**
	 * 场馆类型表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入venueType")
	public R<IPage<VenueTypeVO>> page(VenueTypeVO venueType, Query query) {
		IPage<VenueTypeVO> pages = venueTypeService.selectVenueTypePage(Condition.getPage(query), venueType);
		return R.data(pages);
	}

	/**
	 * 场馆类型表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入venueType")
	public R save(@Valid @RequestBody VenueTypeEntity venueType) {
		return R.status(venueTypeService.save(venueType));
	}

	/**
	 * 场馆类型表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入venueType")
	public R update(@Valid @RequestBody VenueTypeEntity venueType) {
		return R.status(venueTypeService.updateById(venueType));
	}

	/**
	 * 场馆类型表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入venueType")
	public R submit(@Valid @RequestBody VenueTypeEntity venueType) {
		return R.status(venueTypeService.saveOrUpdate(venueType));
	}

	/**
	 * 场馆类型表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(venueTypeService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-venueType")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入venueType")
	public void exportVenueType(@Parameter(hidden = true) @RequestParam Map<String, Object> venueType, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<VenueTypeEntity> queryWrapper = Condition.getQueryWrapper(venueType, VenueTypeEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(VenueType::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(VenueTypeEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<VenueTypeExcel> list = venueTypeService.exportVenueType(queryWrapper);
		ExcelUtil.export(response, "场馆类型表数据" + DateUtil.time(), "场馆类型表数据表", list, VenueTypeExcel.class);
	}

}
