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
package org.springblade.modules.venue.controller;

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
import org.springblade.modules.venue.pojo.entity.VenueEntity;
import org.springblade.modules.venue.pojo.vo.VenueVO;
import org.springblade.modules.venue.excel.VenueExcel;
import org.springblade.modules.venue.wrapper.VenueWrapper;
import org.springblade.modules.venue.service.IVenueService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 体育场馆表 控制器
 *
 * @author BladeX
 * @since 2026-03-10
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-venue/venue")
@Tag(name = "体育场馆表", description = "体育场馆表接口")
public class VenueController extends BladeController {

	private final IVenueService venueService;

	/**
	 * 体育场馆表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入venue")
	public R<VenueVO> detail(VenueEntity venue) {
		VenueEntity detail = venueService.getOne(Condition.getQueryWrapper(venue));
		return R.data(VenueWrapper.build().entityVO(detail));
	}
	/**
	 * 体育场馆表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入venue")
	public R<IPage<VenueVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> venue, Query query) {
		IPage<VenueEntity> pages = venueService.page(Condition.getPage(query), Condition.getQueryWrapper(venue, VenueEntity.class));
		return R.data(VenueWrapper.build().pageVO(pages));
	}

	/**
	 * 体育场馆表 分页
	 */
	@GetMapping("/listDic")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入venue")
	public R<List<VenueEntity>> listDic(@Parameter(hidden = true) @RequestParam Map<String, Object> venue) {

		return R.data(venueService.list());
	}

	/**
	 * 体育场馆表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入venue")
	public R<IPage<VenueVO>> page(VenueVO venue, Query query) {
		IPage<VenueVO> pages = venueService.selectVenuePage(Condition.getPage(query), venue);
		return R.data(pages);
	}

	/**
	 * 体育场馆表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入venue")
	public R save(@Valid @RequestBody VenueEntity venue) {
		return R.status(venueService.save(venue));
	}

	/**
	 * 体育场馆表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入venue")
	public R update(@Valid @RequestBody VenueEntity venue) {
		return R.status(venueService.updateById(venue));
	}

	/**
	 * 体育场馆表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入venue")
	public R submit(@Valid @RequestBody VenueEntity venue) {
		return R.status(venueService.saveOrUpdate(venue));
	}

	/**
	 * 体育场馆表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(venueService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-venue")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入venue")
	public void exportVenue(@Parameter(hidden = true) @RequestParam Map<String, Object> venue, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<VenueEntity> queryWrapper = Condition.getQueryWrapper(venue, VenueEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(Venue::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(VenueEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<VenueExcel> list = venueService.exportVenue(queryWrapper);
		ExcelUtil.export(response, "体育场馆表数据" + DateUtil.time(), "体育场馆表数据表", list, VenueExcel.class);
	}

}
