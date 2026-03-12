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
package org.springblade.modules.newscategory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springblade.modules.newscategory.pojo.entity.NewsCategoryEntity;
import org.springblade.modules.newscategory.pojo.vo.NewsCategoryVO;
import org.springblade.modules.newscategory.excel.NewsCategoryExcel;
import org.springblade.modules.newscategory.wrapper.NewsCategoryWrapper;
import org.springblade.modules.newscategory.service.INewsCategoryService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 新闻分类表 控制器
 *
 * @author BladeX
 * @since 2026-03-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-newscategory/newsCategory")
@Tag(name = "新闻分类表", description = "新闻分类表接口")
public class NewsCategoryController extends BladeController {

	private final INewsCategoryService newsCategoryService;

	/**
	 * 新闻分类表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入newsCategory")
	public R<NewsCategoryVO> detail(NewsCategoryEntity newsCategory) {
		NewsCategoryEntity detail = newsCategoryService.getOne(Condition.getQueryWrapper(newsCategory));
		return R.data(NewsCategoryWrapper.build().entityVO(detail));
	}
	/**
	 * 新闻分类表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入newsCategory")
	public R<IPage<NewsCategoryVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> newsCategory, Query query) {
		IPage<NewsCategoryEntity> pages = newsCategoryService.page(Condition.getPage(query), Condition.getQueryWrapper(newsCategory, NewsCategoryEntity.class));
		return R.data(NewsCategoryWrapper.build().pageVO(pages));
	}

	/**
	 * 新闻分类表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入newsCategory")
	public R<IPage<NewsCategoryVO>> page(NewsCategoryVO newsCategory, Query query) {
		IPage<NewsCategoryVO> pages = newsCategoryService.selectNewsCategoryPage(Condition.getPage(query), newsCategory);
		return R.data(pages);
	}

	/**
	 * 新闻分类表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入newsCategory")
	public R save(@Valid @RequestBody NewsCategoryEntity newsCategory) {
		return R.status(newsCategoryService.save(newsCategory));
	}

	/**
	 * 新闻分类表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入newsCategory")
	public R update(@Valid @RequestBody NewsCategoryEntity newsCategory) {
		return R.status(newsCategoryService.updateById(newsCategory));
	}

	/**
	 * 新闻分类表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入newsCategory")
	public R submit(@Valid @RequestBody NewsCategoryEntity newsCategory) {
		return R.status(newsCategoryService.saveOrUpdate(newsCategory));
	}

	/**
	 * 新闻分类表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(newsCategoryService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-newsCategory")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入newsCategory")
	public void exportNewsCategory(@Parameter(hidden = true) @RequestParam Map<String, Object> newsCategory, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<NewsCategoryEntity> queryWrapper = Condition.getQueryWrapper(newsCategory, NewsCategoryEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(NewsCategory::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(NewsCategoryEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<NewsCategoryExcel> list = newsCategoryService.exportNewsCategory(queryWrapper);
		ExcelUtil.export(response, "新闻分类表数据" + DateUtil.time(), "新闻分类表数据表", list, NewsCategoryExcel.class);
	}

	/**
	 * 类型表	 详情
	 */
	@GetMapping("/type")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "")
	public R partyNewsTypeTypeList1() {
		return R.data(newsCategoryService.list());
	}


}
