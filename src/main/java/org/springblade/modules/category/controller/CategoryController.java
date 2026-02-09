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
package org.springblade.modules.category.controller;

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

import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.category.pojo.entity.CategoryEntity;
import org.springblade.modules.category.pojo.vo.CategoryVO;
import org.springblade.modules.category.excel.CategoryExcel;
import org.springblade.modules.category.wrapper.CategoryWrapper;
import org.springblade.modules.category.service.ICategoryService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 分类表 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-category/category")
@Tag(name = "分类表", description = "分类表接口")
public class CategoryController extends BladeController {

	private final ICategoryService categoryService;

	/**
	 * 分类表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入category")
	public R<CategoryVO> detail(CategoryEntity category) {
		CategoryEntity detail = categoryService.getOne(Condition.getQueryWrapper(category));
		return R.data(CategoryWrapper.build().entityVO(detail));
	}
	/**
	 * 分类表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入category")
	public R<IPage<CategoryVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> category, Query query) {
		IPage<CategoryEntity> pages = categoryService.page(Condition.getPage(query), Condition.getQueryWrapper(category, CategoryEntity.class));
		return R.data(CategoryWrapper.build().pageVO(pages));
	}

	/**
	 * 分类表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入category")
	public R<IPage<CategoryVO>> page(CategoryVO category, Query query) {
		IPage<CategoryVO> pages = categoryService.selectCategoryPage(Condition.getPage(query), category);
		return R.data(pages);
	}

	/**
	 * 分类表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入category")
	public R save(@Valid @RequestBody CategoryEntity category) {
		return R.status(categoryService.save(category));
	}

	/**
	 * 分类表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入category")
	public R update(@Valid @RequestBody CategoryEntity category) {
		return R.status(categoryService.updateById(category));
	}

	/**
	 * 分类表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入category")
	public R submit(@Valid @RequestBody CategoryEntity category) {
		return R.status(categoryService.saveOrUpdate(category));
	}

	/**
	 * 分类表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(categoryService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-category")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入category")
	public void exportCategory(@Parameter(hidden = true) @RequestParam Map<String, Object> category, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<CategoryEntity> queryWrapper = Condition.getQueryWrapper(category, CategoryEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(Category::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(CategoryEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<CategoryExcel> list = categoryService.exportCategory(queryWrapper);
		ExcelUtil.export(response, "分类表数据" + DateUtil.time(), "分类表数据表", list, CategoryExcel.class);
	}


	/**
	 * 得到所有分类，返回树形结构
	 */
	@GetMapping("/getTreeCategory")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "得到所有分类", description = "返回树形结构")
	public R<List<CategoryVO>> getTreeCategory() {
		return R.data(categoryService.getTreeCategory());
	}

	/**
	 * 通过分类获取所有的图片
	 */
	@GetMapping("/getImgListByCategory")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "通过分类获取所有的图片", description = "传入page, limit, id, type")
	public R<IPage<ImgDetailVO>> getImgListByCategory(Query query, @RequestParam String id, @RequestParam(required = false) Integer type) {
		IPage<ImgDetailVO> page = Condition.getPage(query);
		return R.data(categoryService.getImgListByCategory(page, id, type));
	}

}
