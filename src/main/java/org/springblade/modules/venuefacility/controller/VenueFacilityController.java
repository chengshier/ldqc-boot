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
package org.springblade.modules.venuefacility.controller;

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
import org.springblade.modules.venuefacility.pojo.entity.VenueFacilityEntity;
import org.springblade.modules.venuefacility.pojo.vo.VenueFacilityVO;
import org.springblade.modules.venuefacility.excel.VenueFacilityExcel;
import org.springblade.modules.venuefacility.wrapper.VenueFacilityWrapper;
import org.springblade.modules.venuefacility.service.IVenueFacilityService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 场馆设施表 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-venuefacility/venueFacility")
@Tag(name = "场馆设施表", description = "场馆设施表接口")
public class VenueFacilityController extends BladeController {

	private final IVenueFacilityService venueFacilityService;

	/**
	 * 场馆设施表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入venueFacility")
	public R<VenueFacilityVO> detail(VenueFacilityEntity venueFacility) {
		VenueFacilityEntity detail = venueFacilityService.getOne(Condition.getQueryWrapper(venueFacility));
		return R.data(VenueFacilityWrapper.build().entityVO(detail));
	}
	/**
	 * 场馆设施表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入venueFacility")
	public R<IPage<VenueFacilityVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> venueFacility, Query query) {
		IPage<VenueFacilityEntity> pages = venueFacilityService.page(Condition.getPage(query), Condition.getQueryWrapper(venueFacility, VenueFacilityEntity.class));
		return R.data(VenueFacilityWrapper.build().pageVO(pages));
	}

	/**
	 * 场馆设施表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入venueFacility")
	public R<IPage<VenueFacilityVO>> page(VenueFacilityVO venueFacility, Query query) {
		IPage<VenueFacilityVO> pages = venueFacilityService.selectVenueFacilityPage(Condition.getPage(query), venueFacility);
		return R.data(pages);
	}

	/**
	 * 场馆设施表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入venueFacility")
	public R save(@Valid @RequestBody VenueFacilityEntity venueFacility) {
		return R.status(venueFacilityService.save(venueFacility));
	}

	/**
	 * 场馆设施表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入venueFacility")
	public R update(@Valid @RequestBody VenueFacilityEntity venueFacility) {
		return R.status(venueFacilityService.updateById(venueFacility));
	}

	/**
	 * 场馆设施表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入venueFacility")
	public R submit(@Valid @RequestBody VenueFacilityEntity venueFacility) {
		return R.status(venueFacilityService.saveOrUpdate(venueFacility));
	}

	/**
	 * 场馆设施表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(venueFacilityService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-venueFacility")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入venueFacility")
	public void exportVenueFacility(@Parameter(hidden = true) @RequestParam Map<String, Object> venueFacility, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<VenueFacilityEntity> queryWrapper = Condition.getQueryWrapper(venueFacility, VenueFacilityEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(VenueFacility::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(VenueFacilityEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<VenueFacilityExcel> list = venueFacilityService.exportVenueFacility(queryWrapper);
		ExcelUtil.export(response, "场馆设施表数据" + DateUtil.time(), "场馆设施表数据表", list, VenueFacilityExcel.class);
	}

}
