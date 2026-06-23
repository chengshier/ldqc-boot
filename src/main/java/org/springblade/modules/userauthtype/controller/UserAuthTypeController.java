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
package org.springblade.modules.userauthtype.controller;

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
import org.springblade.modules.userauthtype.pojo.entity.UserAuthTypeEntity;
import org.springblade.modules.userauthtype.pojo.vo.UserAuthTypeVO;
import org.springblade.modules.userauthtype.excel.UserAuthTypeExcel;
import org.springblade.modules.userauthtype.wrapper.UserAuthTypeWrapper;
import org.springblade.modules.userauthtype.service.IUserAuthTypeService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户认证类型表 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-userauthtype/userAuthType")
@Tag(name = "用户认证类型表", description = "用户认证类型表接口")
public class UserAuthTypeController extends BladeController {

	private final IUserAuthTypeService userAuthTypeService;

	/**
	 * 用户认证类型表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入userAuthType")
	public R<UserAuthTypeVO> detail(UserAuthTypeEntity userAuthType) {
		UserAuthTypeEntity detail = userAuthTypeService.getOne(Condition.getQueryWrapper(userAuthType));
		return R.data(UserAuthTypeWrapper.build().entityVO(detail));
	}
	/**
	 * 用户认证类型表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入userAuthType")
	public R<IPage<UserAuthTypeVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> userAuthType, Query query) {
		IPage<UserAuthTypeEntity> pages = userAuthTypeService.page(Condition.getPage(query), Condition.getQueryWrapper(userAuthType, UserAuthTypeEntity.class));
		return R.data(UserAuthTypeWrapper.build().pageVO(pages));
	}

	/**
	 * 用户认证类型表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入userAuthType")
	public R<IPage<UserAuthTypeVO>> page(UserAuthTypeVO userAuthType, Query query) {
		IPage<UserAuthTypeVO> pages = userAuthTypeService.selectUserAuthTypePage(Condition.getPage(query), userAuthType);
		return R.data(pages);
	}

	/**
	 * 用户认证类型表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入userAuthType")
	public R save(@Valid @RequestBody UserAuthTypeEntity userAuthType) {
		return R.status(userAuthTypeService.save(userAuthType));
	}

	/**
	 * 用户认证类型表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入userAuthType")
	public R update(@Valid @RequestBody UserAuthTypeEntity userAuthType) {
		return R.status(userAuthTypeService.updateById(userAuthType));
	}

	/**
	 * 用户认证类型表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入userAuthType")
	public R submit(@Valid @RequestBody UserAuthTypeEntity userAuthType) {
		return R.status(userAuthTypeService.saveOrUpdate(userAuthType));
	}

	/**
	 * 用户认证类型表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(userAuthTypeService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-userAuthType")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入userAuthType")
	public void exportUserAuthType(@Parameter(hidden = true) @RequestParam Map<String, Object> userAuthType, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<UserAuthTypeEntity> queryWrapper = Condition.getQueryWrapper(userAuthType, UserAuthTypeEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(UserAuthType::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(UserAuthTypeEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<UserAuthTypeExcel> list = userAuthTypeService.exportUserAuthType(queryWrapper);
		ExcelUtil.export(response, "用户认证类型表数据" + DateUtil.time(), "用户认证类型表数据表", list, UserAuthTypeExcel.class);
	}

}
