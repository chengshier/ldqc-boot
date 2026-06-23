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
package org.springblade.modules.outdoorsignup.controller;

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
import org.springblade.modules.outdoorsignup.pojo.entity.OutdoorSignupEntity;
import org.springblade.modules.outdoorsignup.pojo.vo.OutdoorSignupVO;
import org.springblade.modules.outdoorsignup.excel.OutdoorSignupExcel;
import org.springblade.modules.outdoorsignup.wrapper.OutdoorSignupWrapper;
import org.springblade.modules.outdoorsignup.service.IOutdoorSignupService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 户外报名表 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-outdoorsignup/outdoorSignup")
@Tag(name = "户外报名表", description = "户外报名表接口")
public class OutdoorSignupController extends BladeController {

	private final IOutdoorSignupService outdoorSignupService;

	/**
	 * 户外报名表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入outdoorSignup")
	public R<OutdoorSignupVO> detail(OutdoorSignupEntity outdoorSignup) {
		OutdoorSignupEntity detail = outdoorSignupService.getOne(Condition.getQueryWrapper(outdoorSignup));
		return R.data(OutdoorSignupWrapper.build().entityVO(detail));
	}
	/**
	 * 户外报名表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入outdoorSignup")
	public R<IPage<OutdoorSignupVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> outdoorSignup, Query query) {
		IPage<OutdoorSignupEntity> pages = outdoorSignupService.page(Condition.getPage(query), Condition.getQueryWrapper(outdoorSignup, OutdoorSignupEntity.class));
		return R.data(OutdoorSignupWrapper.build().pageVO(pages));
	}

	/**
	 * 户外报名表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入outdoorSignup")
	public R<IPage<OutdoorSignupVO>> page(OutdoorSignupVO outdoorSignup, Query query) {
		IPage<OutdoorSignupVO> pages = outdoorSignupService.selectOutdoorSignupPage(Condition.getPage(query), outdoorSignup);
		return R.data(pages);
	}

	/**
	 * 户外报名表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入outdoorSignup")
	public R save(@Valid @RequestBody OutdoorSignupEntity outdoorSignup) {
		return R.status(outdoorSignupService.save(outdoorSignup));
	}

	/**
	 * 户外报名表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入outdoorSignup")
	public R update(@Valid @RequestBody OutdoorSignupEntity outdoorSignup) {
		return R.status(outdoorSignupService.updateById(outdoorSignup));
	}

	/**
	 * 户外报名表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入outdoorSignup")
	public R submit(@Valid @RequestBody OutdoorSignupEntity outdoorSignup) {
		return R.status(outdoorSignupService.saveOrUpdate(outdoorSignup));
	}

	/**
	 * 户外报名表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(outdoorSignupService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-outdoorSignup")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入outdoorSignup")
	public void exportOutdoorSignup(@Parameter(hidden = true) @RequestParam Map<String, Object> outdoorSignup, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<OutdoorSignupEntity> queryWrapper = Condition.getQueryWrapper(outdoorSignup, OutdoorSignupEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(OutdoorSignup::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(OutdoorSignupEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<OutdoorSignupExcel> list = outdoorSignupService.exportOutdoorSignup(queryWrapper);
		ExcelUtil.export(response, "户外报名表数据" + DateUtil.time(), "户外报名表数据表", list, OutdoorSignupExcel.class);
	}

}
