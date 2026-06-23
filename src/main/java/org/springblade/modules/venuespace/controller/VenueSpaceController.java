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
package org.springblade.modules.venuespace.controller;

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
import org.springblade.modules.venuespace.pojo.entity.VenueSpaceEntity;
import org.springblade.modules.venuespace.pojo.vo.VenueSpaceVO;
import org.springblade.modules.venuespace.excel.VenueSpaceExcel;
import org.springblade.modules.venuespace.wrapper.VenueSpaceWrapper;
import org.springblade.modules.venuespace.service.IVenueSpaceService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 场馆场地表 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-venuespace/venueSpace")
@Tag(name = "场馆场地表", description = "场馆场地表接口")
public class VenueSpaceController extends BladeController {

	private final IVenueSpaceService venueSpaceService;

	/**
	 * 场馆场地表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入venueSpace")
	public R<VenueSpaceVO> detail(VenueSpaceEntity venueSpace) {
		VenueSpaceEntity detail = venueSpaceService.getOne(Condition.getQueryWrapper(venueSpace));
		return R.data(VenueSpaceWrapper.build().entityVO(detail));
	}
	/**
	 * 场馆场地表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入venueSpace")
	public R<IPage<VenueSpaceVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> venueSpace, Query query) {
		IPage<VenueSpaceEntity> pages = venueSpaceService.page(Condition.getPage(query), Condition.getQueryWrapper(venueSpace, VenueSpaceEntity.class));
		return R.data(VenueSpaceWrapper.build().pageVO(pages));
	}

	/**
	 * 场馆场地表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入venueSpace")
	public R<IPage<VenueSpaceVO>> page(VenueSpaceVO venueSpace, Query query) {
		IPage<VenueSpaceVO> pages = venueSpaceService.selectVenueSpacePage(Condition.getPage(query), venueSpace);
		return R.data(pages);
	}

	/**
	 * 场馆场地表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入venueSpace")
	public R save(@Valid @RequestBody VenueSpaceEntity venueSpace) {
		return R.status(venueSpaceService.save(venueSpace));
	}

	/**
	 * 场馆场地表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入venueSpace")
	public R update(@Valid @RequestBody VenueSpaceEntity venueSpace) {
		return R.status(venueSpaceService.updateById(venueSpace));
	}

	/**
	 * 场馆场地表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入venueSpace")
	public R submit(@Valid @RequestBody VenueSpaceEntity venueSpace) {
		return R.status(venueSpaceService.saveOrUpdate(venueSpace));
	}

	/**
	 * 场馆场地表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(venueSpaceService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-venueSpace")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入venueSpace")
	public void exportVenueSpace(@Parameter(hidden = true) @RequestParam Map<String, Object> venueSpace, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<VenueSpaceEntity> queryWrapper = Condition.getQueryWrapper(venueSpace, VenueSpaceEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(VenueSpace::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(VenueSpaceEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<VenueSpaceExcel> list = venueSpaceService.exportVenueSpace(queryWrapper);
		ExcelUtil.export(response, "场馆场地表数据" + DateUtil.time(), "场馆场地表数据表", list, VenueSpaceExcel.class);
	}

}
