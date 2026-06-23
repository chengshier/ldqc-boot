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
package org.springblade.modules.userauthfile.controller;

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
import org.springblade.modules.userauthfile.pojo.entity.UserAuthFileEntity;
import org.springblade.modules.userauthfile.pojo.vo.UserAuthFileVO;
import org.springblade.modules.userauthfile.excel.UserAuthFileExcel;
import org.springblade.modules.userauthfile.wrapper.UserAuthFileWrapper;
import org.springblade.modules.userauthfile.service.IUserAuthFileService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户认证附件表 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-userauthfile/userAuthFile")
@Tag(name = "用户认证附件表", description = "用户认证附件表接口")
public class UserAuthFileController extends BladeController {

	private final IUserAuthFileService userAuthFileService;

	/**
	 * 用户认证附件表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入userAuthFile")
	public R<UserAuthFileVO> detail(UserAuthFileEntity userAuthFile) {
		UserAuthFileEntity detail = userAuthFileService.getOne(Condition.getQueryWrapper(userAuthFile));
		return R.data(UserAuthFileWrapper.build().entityVO(detail));
	}
	/**
	 * 用户认证附件表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入userAuthFile")
	public R<IPage<UserAuthFileVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> userAuthFile, Query query) {
		IPage<UserAuthFileEntity> pages = userAuthFileService.page(Condition.getPage(query), Condition.getQueryWrapper(userAuthFile, UserAuthFileEntity.class));
		return R.data(UserAuthFileWrapper.build().pageVO(pages));
	}

	/**
	 * 用户认证附件表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入userAuthFile")
	public R<IPage<UserAuthFileVO>> page(UserAuthFileVO userAuthFile, Query query) {
		IPage<UserAuthFileVO> pages = userAuthFileService.selectUserAuthFilePage(Condition.getPage(query), userAuthFile);
		return R.data(pages);
	}

	/**
	 * 用户认证附件表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入userAuthFile")
	public R save(@Valid @RequestBody UserAuthFileEntity userAuthFile) {
		return R.status(userAuthFileService.save(userAuthFile));
	}

	/**
	 * 用户认证附件表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入userAuthFile")
	public R update(@Valid @RequestBody UserAuthFileEntity userAuthFile) {
		return R.status(userAuthFileService.updateById(userAuthFile));
	}

	/**
	 * 用户认证附件表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入userAuthFile")
	public R submit(@Valid @RequestBody UserAuthFileEntity userAuthFile) {
		return R.status(userAuthFileService.saveOrUpdate(userAuthFile));
	}

	/**
	 * 用户认证附件表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(userAuthFileService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-userAuthFile")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入userAuthFile")
	public void exportUserAuthFile(@Parameter(hidden = true) @RequestParam Map<String, Object> userAuthFile, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<UserAuthFileEntity> queryWrapper = Condition.getQueryWrapper(userAuthFile, UserAuthFileEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(UserAuthFile::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(UserAuthFileEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<UserAuthFileExcel> list = userAuthFileService.exportUserAuthFile(queryWrapper);
		ExcelUtil.export(response, "用户认证附件表数据" + DateUtil.time(), "用户认证附件表数据表", list, UserAuthFileExcel.class);
	}

}
