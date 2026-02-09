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
package org.springblade.modules.tagimgrelation.controller;

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
import org.springblade.modules.tagimgrelation.pojo.entity.TagImgRelationEntity;
import org.springblade.modules.tagimgrelation.pojo.vo.TagImgRelationVO;
import org.springblade.modules.tagimgrelation.excel.TagImgRelationExcel;
import org.springblade.modules.tagimgrelation.wrapper.TagImgRelationWrapper;
import org.springblade.modules.tagimgrelation.service.ITagImgRelationService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 标签图片关系表 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-tagimgrelation/tagImgRelation")
@Tag(name = "标签图片关系表", description = "标签图片关系表接口")
public class TagImgRelationController extends BladeController {

	private final ITagImgRelationService tagImgRelationService;

	/**
	 * 标签图片关系表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入tagImgRelation")
	public R<TagImgRelationVO> detail(TagImgRelationEntity tagImgRelation) {
		TagImgRelationEntity detail = tagImgRelationService.getOne(Condition.getQueryWrapper(tagImgRelation));
		return R.data(TagImgRelationWrapper.build().entityVO(detail));
	}
	/**
	 * 标签图片关系表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入tagImgRelation")
	public R<IPage<TagImgRelationVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> tagImgRelation, Query query) {
		IPage<TagImgRelationEntity> pages = tagImgRelationService.page(Condition.getPage(query), Condition.getQueryWrapper(tagImgRelation, TagImgRelationEntity.class));
		return R.data(TagImgRelationWrapper.build().pageVO(pages));
	}

	/**
	 * 标签图片关系表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入tagImgRelation")
	public R<IPage<TagImgRelationVO>> page(TagImgRelationVO tagImgRelation, Query query) {
		IPage<TagImgRelationVO> pages = tagImgRelationService.selectTagImgRelationPage(Condition.getPage(query), tagImgRelation);
		return R.data(pages);
	}

	/**
	 * 标签图片关系表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入tagImgRelation")
	public R save(@Valid @RequestBody TagImgRelationEntity tagImgRelation) {
		return R.status(tagImgRelationService.save(tagImgRelation));
	}

	/**
	 * 标签图片关系表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入tagImgRelation")
	public R update(@Valid @RequestBody TagImgRelationEntity tagImgRelation) {
		return R.status(tagImgRelationService.updateById(tagImgRelation));
	}

	/**
	 * 标签图片关系表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入tagImgRelation")
	public R submit(@Valid @RequestBody TagImgRelationEntity tagImgRelation) {
		return R.status(tagImgRelationService.saveOrUpdate(tagImgRelation));
	}

	/**
	 * 标签图片关系表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(tagImgRelationService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-tagImgRelation")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入tagImgRelation")
	public void exportTagImgRelation(@Parameter(hidden = true) @RequestParam Map<String, Object> tagImgRelation, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<TagImgRelationEntity> queryWrapper = Condition.getQueryWrapper(tagImgRelation, TagImgRelationEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(TagImgRelation::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(TagImgRelationEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<TagImgRelationExcel> list = tagImgRelationService.exportTagImgRelation(queryWrapper);
		ExcelUtil.export(response, "标签图片关系表数据" + DateUtil.time(), "标签图片关系表数据表", list, TagImgRelationExcel.class);
	}

}
