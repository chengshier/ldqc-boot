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
package org.springblade.modules.talentpost.controller;

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
import org.springblade.modules.talentpost.pojo.entity.TalentPostEntity;
import org.springblade.modules.talentpost.pojo.vo.TalentPostVO;
import org.springblade.modules.talentpost.excel.TalentPostExcel;
import org.springblade.modules.talentpost.wrapper.TalentPostWrapper;
import org.springblade.modules.talentpost.service.ITalentPostService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 达人动态表 控制器
 *
 * @author BladeX
 * @since 2026-03-11
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-talentpost/talentPost")
@Tag(name = "达人动态表", description = "达人动态表接口")
public class TalentPostController extends BladeController {

	private final ITalentPostService talentPostService;

	/**
	 * 达人动态表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入talentPost")
	public R<TalentPostVO> detail(TalentPostEntity talentPost) {
		TalentPostEntity detail = talentPostService.getOne(Condition.getQueryWrapper(talentPost));
		return R.data(TalentPostWrapper.build().entityVO(detail));
	}
	/**
	 * 达人动态表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入talentPost")
	public R<IPage<TalentPostVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> talentPost, Query query) {
		IPage<TalentPostEntity> pages = talentPostService.page(Condition.getPage(query), Condition.getQueryWrapper(talentPost, TalentPostEntity.class));
		return R.data(TalentPostWrapper.build().pageVO(pages));
	}

	/**
	 * 达人动态表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入talentPost")
	public R<IPage<TalentPostVO>> page(TalentPostVO talentPost, Query query) {
		IPage<TalentPostVO> pages = talentPostService.selectTalentPostPage(Condition.getPage(query), talentPost);
		return R.data(pages);
	}

	/**
	 * 达人动态表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入talentPost")
	public R save(@Valid @RequestBody TalentPostEntity talentPost) {
		return R.status(talentPostService.save(talentPost));
	}

	/**
	 * 达人动态表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入talentPost")
	public R update(@Valid @RequestBody TalentPostEntity talentPost) {
		return R.status(talentPostService.updateById(talentPost));
	}

	/**
	 * 达人动态表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入talentPost")
	public R submit(@Valid @RequestBody TalentPostEntity talentPost) {
		return R.status(talentPostService.saveOrUpdate(talentPost));
	}

	/**
	 * 达人动态表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(talentPostService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-talentPost")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入talentPost")
	public void exportTalentPost(@Parameter(hidden = true) @RequestParam Map<String, Object> talentPost, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<TalentPostEntity> queryWrapper = Condition.getQueryWrapper(talentPost, TalentPostEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(TalentPost::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(TalentPostEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<TalentPostExcel> list = talentPostService.exportTalentPost(queryWrapper);
		ExcelUtil.export(response, "达人动态表数据" + DateUtil.time(), "达人动态表数据表", list, TalentPostExcel.class);
	}

}
