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
package org.springblade.modules.userthree.controller;

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
import org.springblade.modules.userthree.pojo.entity.UserThreeEntity;
import org.springblade.modules.userthree.pojo.vo.UserThreeVO;
import org.springblade.modules.userthree.excel.UserThreeExcel;
import org.springblade.modules.userthree.wrapper.UserThreeWrapper;
import org.springblade.modules.userthree.service.IUserThreeService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户微信登录认证表 控制器
 *
 * @author BladeX
 * @since 2026-02-04
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-userthree/userThree")
@Tag(name = "用户微信登录认证表", description = "用户微信登录认证表接口")
public class UserThreeController extends BladeController {

	private final IUserThreeService userThreeService;

	/**
	 * 用户微信登录认证表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入userThree")
	public R<UserThreeVO> detail(UserThreeEntity userThree) {
		UserThreeEntity detail = userThreeService.getOne(Condition.getQueryWrapper(userThree));
		return R.data(UserThreeWrapper.build().entityVO(detail));
	}
	/**
	 * 用户微信登录认证表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入userThree")
	public R<IPage<UserThreeVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> userThree, Query query) {
		IPage<UserThreeEntity> pages = userThreeService.page(Condition.getPage(query), Condition.getQueryWrapper(userThree, UserThreeEntity.class));
		return R.data(UserThreeWrapper.build().pageVO(pages));
	}

	/**
	 * 用户微信登录认证表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入userThree")
	public R<IPage<UserThreeVO>> page(UserThreeVO userThree, Query query) {
		IPage<UserThreeVO> pages = userThreeService.selectUserThreePage(Condition.getPage(query), userThree);
		return R.data(pages);
	}

	/**
	 * 用户微信登录认证表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入userThree")
	public R save(@Valid @RequestBody UserThreeEntity userThree) {
		return R.status(userThreeService.save(userThree));
	}

	/**
	 * 用户微信登录认证表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入userThree")
	public R update(@Valid @RequestBody UserThreeEntity userThree) {
		return R.status(userThreeService.updateById(userThree));
	}

	/**
	 * 用户微信登录认证表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入userThree")
	public R submit(@Valid @RequestBody UserThreeEntity userThree) {
		return R.status(userThreeService.saveOrUpdate(userThree));
	}

	/**
	 * 用户微信登录认证表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(userThreeService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-userThree")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入userThree")
	public void exportUserThree(@Parameter(hidden = true) @RequestParam Map<String, Object> userThree, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<UserThreeEntity> queryWrapper = Condition.getQueryWrapper(userThree, UserThreeEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(UserThree::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(UserThreeEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<UserThreeExcel> list = userThreeService.exportUserThree(queryWrapper);
		ExcelUtil.export(response, "用户微信登录认证表数据" + DateUtil.time(), "用户微信登录认证表数据表", list, UserThreeExcel.class);
	}

}
