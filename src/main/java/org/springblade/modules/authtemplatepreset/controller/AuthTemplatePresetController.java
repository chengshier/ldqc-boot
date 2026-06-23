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
package org.springblade.modules.authtemplatepreset.controller;

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
import org.springblade.modules.authtemplatepreset.pojo.entity.AuthTemplatePresetEntity;
import org.springblade.modules.authtemplatepreset.pojo.vo.AuthTemplatePresetVO;
import org.springblade.modules.authtemplatepreset.excel.AuthTemplatePresetExcel;
import org.springblade.modules.authtemplatepreset.wrapper.AuthTemplatePresetWrapper;
import org.springblade.modules.authtemplatepreset.service.IAuthTemplatePresetService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证模板推荐项(字段/附件) 控制器
 *
 * @author BladeX
 * @since 2026-04-09
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-authtemplatepreset/authTemplatePreset")
@Tag(name = "认证模板推荐项(字段/附件)", description = "认证模板推荐项(字段/附件)接口")
public class AuthTemplatePresetController extends BladeController {

	private final IAuthTemplatePresetService authTemplatePresetService;

	/**
	 * 认证模板推荐项(字段/附件) 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入authTemplatePreset")
	public R<AuthTemplatePresetVO> detail(AuthTemplatePresetEntity authTemplatePreset) {
		AuthTemplatePresetEntity detail = authTemplatePresetService.getOne(Condition.getQueryWrapper(authTemplatePreset));
		return R.data(AuthTemplatePresetWrapper.build().entityVO(detail));
	}
	/**
	 * 认证模板推荐项(字段/附件) 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入authTemplatePreset")
	public R<IPage<AuthTemplatePresetVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> authTemplatePreset, Query query) {
		IPage<AuthTemplatePresetEntity> pages = authTemplatePresetService.page(Condition.getPage(query), Condition.getQueryWrapper(authTemplatePreset, AuthTemplatePresetEntity.class));
		return R.data(AuthTemplatePresetWrapper.build().pageVO(pages));
	}

	/**
	 * 认证模板推荐项(字段/附件) 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入authTemplatePreset")
	public R<IPage<AuthTemplatePresetVO>> page(AuthTemplatePresetVO authTemplatePreset, Query query) {
		IPage<AuthTemplatePresetVO> pages = authTemplatePresetService.selectAuthTemplatePresetPage(Condition.getPage(query), authTemplatePreset);
		return R.data(pages);
	}

	/**
	 * 认证模板推荐项(字段/附件) 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入authTemplatePreset")
	public R save(@Valid @RequestBody AuthTemplatePresetEntity authTemplatePreset) {
		return R.status(authTemplatePresetService.save(authTemplatePreset));
	}

	/**
	 * 认证模板推荐项(字段/附件) 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入authTemplatePreset")
	public R update(@Valid @RequestBody AuthTemplatePresetEntity authTemplatePreset) {
		return R.status(authTemplatePresetService.updateById(authTemplatePreset));
	}

	/**
	 * 认证模板推荐项(字段/附件) 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入authTemplatePreset")
	public R submit(@Valid @RequestBody AuthTemplatePresetEntity authTemplatePreset) {
		return R.status(authTemplatePresetService.saveOrUpdate(authTemplatePreset));
	}

	/**
	 * 认证模板推荐项(字段/附件) 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(authTemplatePresetService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-authTemplatePreset")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入authTemplatePreset")
	public void exportAuthTemplatePreset(@Parameter(hidden = true) @RequestParam Map<String, Object> authTemplatePreset, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<AuthTemplatePresetEntity> queryWrapper = Condition.getQueryWrapper(authTemplatePreset, AuthTemplatePresetEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(AuthTemplatePreset::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(AuthTemplatePresetEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<AuthTemplatePresetExcel> list = authTemplatePresetService.exportAuthTemplatePreset(queryWrapper);
		ExcelUtil.export(response, "认证模板推荐项(字段/附件)数据" + DateUtil.time(), "认证模板推荐项(字段/附件)数据表", list, AuthTemplatePresetExcel.class);
	}

}
