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
package org.springblade.modules.userauthaudit.controller;

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
import org.springblade.modules.userauthaudit.pojo.entity.UserAuthAuditEntity;
import org.springblade.modules.userauthaudit.pojo.vo.UserAuthAuditVO;
import org.springblade.modules.userauthaudit.excel.UserAuthAuditExcel;
import org.springblade.modules.userauthaudit.wrapper.UserAuthAuditWrapper;
import org.springblade.modules.userauthaudit.service.IUserAuthAuditService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户认证审核日志表 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-userauthaudit/userAuthAudit")
@Tag(name = "用户认证审核日志表", description = "用户认证审核日志表接口")
public class UserAuthAuditController extends BladeController {

	private final IUserAuthAuditService userAuthAuditService;

	/**
	 * 用户认证审核日志表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入userAuthAudit")
	public R<UserAuthAuditVO> detail(UserAuthAuditEntity userAuthAudit) {
		UserAuthAuditEntity detail = userAuthAuditService.getOne(Condition.getQueryWrapper(userAuthAudit));
		return R.data(UserAuthAuditWrapper.build().entityVO(detail));
	}
	/**
	 * 用户认证审核日志表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入userAuthAudit")
	public R<IPage<UserAuthAuditVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> userAuthAudit, Query query) {
		IPage<UserAuthAuditEntity> pages = userAuthAuditService.page(Condition.getPage(query), Condition.getQueryWrapper(userAuthAudit, UserAuthAuditEntity.class));
		return R.data(UserAuthAuditWrapper.build().pageVO(pages));
	}

	/**
	 * 用户认证审核日志表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入userAuthAudit")
	public R<IPage<UserAuthAuditVO>> page(UserAuthAuditVO userAuthAudit, Query query) {
		IPage<UserAuthAuditVO> pages = userAuthAuditService.selectUserAuthAuditPage(Condition.getPage(query), userAuthAudit);
		return R.data(pages);
	}

	/**
	 * 用户认证审核日志表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入userAuthAudit")
	public R save(@Valid @RequestBody UserAuthAuditEntity userAuthAudit) {
		return R.status(userAuthAuditService.save(userAuthAudit));
	}

	/**
	 * 用户认证审核日志表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入userAuthAudit")
	public R update(@Valid @RequestBody UserAuthAuditEntity userAuthAudit) {
		return R.status(userAuthAuditService.updateById(userAuthAudit));
	}

	/**
	 * 用户认证审核日志表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入userAuthAudit")
	public R submit(@Valid @RequestBody UserAuthAuditEntity userAuthAudit) {
		return R.status(userAuthAuditService.saveOrUpdate(userAuthAudit));
	}

	/**
	 * 用户认证审核日志表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(userAuthAuditService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-userAuthAudit")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入userAuthAudit")
	public void exportUserAuthAudit(@Parameter(hidden = true) @RequestParam Map<String, Object> userAuthAudit, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<UserAuthAuditEntity> queryWrapper = Condition.getQueryWrapper(userAuthAudit, UserAuthAuditEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(UserAuthAudit::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(UserAuthAuditEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<UserAuthAuditExcel> list = userAuthAuditService.exportUserAuthAudit(queryWrapper);
		ExcelUtil.export(response, "用户认证审核日志表数据" + DateUtil.time(), "用户认证审核日志表数据表", list, UserAuthAuditExcel.class);
	}

}
