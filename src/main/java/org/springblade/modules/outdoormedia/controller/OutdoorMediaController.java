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
package org.springblade.modules.outdoormedia.controller;

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
import org.springblade.modules.outdoormedia.pojo.entity.OutdoorMediaEntity;
import org.springblade.modules.outdoormedia.pojo.vo.OutdoorMediaVO;
import org.springblade.modules.outdoormedia.excel.OutdoorMediaExcel;
import org.springblade.modules.outdoormedia.wrapper.OutdoorMediaWrapper;
import org.springblade.modules.outdoormedia.service.IOutdoorMediaService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 户外图集表 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-outdoormedia/outdoorMedia")
@Tag(name = "户外图集表", description = "户外图集表接口")
public class OutdoorMediaController extends BladeController {

	private final IOutdoorMediaService outdoorMediaService;

	/**
	 * 户外图集表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入outdoorMedia")
	public R<OutdoorMediaVO> detail(OutdoorMediaEntity outdoorMedia) {
		OutdoorMediaEntity detail = outdoorMediaService.getOne(Condition.getQueryWrapper(outdoorMedia));
		return R.data(OutdoorMediaWrapper.build().entityVO(detail));
	}
	/**
	 * 户外图集表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入outdoorMedia")
	public R<IPage<OutdoorMediaVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> outdoorMedia, Query query) {
		IPage<OutdoorMediaEntity> pages = outdoorMediaService.page(Condition.getPage(query), Condition.getQueryWrapper(outdoorMedia, OutdoorMediaEntity.class));
		return R.data(OutdoorMediaWrapper.build().pageVO(pages));
	}

	/**
	 * 户外图集表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入outdoorMedia")
	public R<IPage<OutdoorMediaVO>> page(OutdoorMediaVO outdoorMedia, Query query) {
		IPage<OutdoorMediaVO> pages = outdoorMediaService.selectOutdoorMediaPage(Condition.getPage(query), outdoorMedia);
		return R.data(pages);
	}

	/**
	 * 户外图集表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入outdoorMedia")
	public R save(@Valid @RequestBody OutdoorMediaEntity outdoorMedia) {
		return R.status(outdoorMediaService.save(outdoorMedia));
	}

	/**
	 * 户外图集表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入outdoorMedia")
	public R update(@Valid @RequestBody OutdoorMediaEntity outdoorMedia) {
		return R.status(outdoorMediaService.updateById(outdoorMedia));
	}

	/**
	 * 户外图集表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入outdoorMedia")
	public R submit(@Valid @RequestBody OutdoorMediaEntity outdoorMedia) {
		return R.status(outdoorMediaService.saveOrUpdate(outdoorMedia));
	}

	/**
	 * 户外图集表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(outdoorMediaService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-outdoorMedia")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入outdoorMedia")
	public void exportOutdoorMedia(@Parameter(hidden = true) @RequestParam Map<String, Object> outdoorMedia, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<OutdoorMediaEntity> queryWrapper = Condition.getQueryWrapper(outdoorMedia, OutdoorMediaEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(OutdoorMedia::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(OutdoorMediaEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<OutdoorMediaExcel> list = outdoorMediaService.exportOutdoorMedia(queryWrapper);
		ExcelUtil.export(response, "户外图集表数据" + DateUtil.time(), "户外图集表数据表", list, OutdoorMediaExcel.class);
	}

}
