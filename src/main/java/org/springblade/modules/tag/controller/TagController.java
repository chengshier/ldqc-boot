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
package org.springblade.modules.tag.controller;

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
import org.springblade.modules.tag.pojo.dto.TagDTO;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.tag.pojo.entity.TagEntity;
import org.springblade.modules.tag.pojo.vo.TagVO;
import org.springblade.modules.tag.excel.TagExcel;
import org.springblade.modules.tag.wrapper.TagWrapper;
import org.springblade.modules.tag.service.ITagService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 标签表 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-tag/tag")
@Tag(name = "标签表", description = "标签表接口")
public class TagController extends BladeController {

	private final ITagService tagService;

	/**
	 * 标签表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入tag")
	public R<TagVO> detail(TagEntity tag) {
		TagEntity detail = tagService.getOne(Condition.getQueryWrapper(tag));
		return R.data(TagWrapper.build().entityVO(detail));
	}
	/**
	 * 标签表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入tag")
	public R<IPage<TagVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> tag, Query query) {
		IPage<TagEntity> pages = tagService.page(Condition.getPage(query), Condition.getQueryWrapper(tag, TagEntity.class));
		return R.data(TagWrapper.build().pageVO(pages));
	}

	/**
	 * 标签表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入tag")
	public R<IPage<TagVO>> page(TagVO tag, Query query) {
		IPage<TagVO> pages = tagService.selectTagPage(Condition.getPage(query), tag);
		return R.data(pages);
	}

	/**
	 * 标签表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入tag")
	public R save(@Valid @RequestBody TagEntity tag) {
		return R.status(tagService.save(tag));
	}

	/**
	 * 标签表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入tag")
	public R update(@Valid @RequestBody TagEntity tag) {
		return R.status(tagService.updateById(tag));
	}

	/**
	 * 标签表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入tag")
	public R submit(@Valid @RequestBody TagEntity tag) {
		return R.status(tagService.saveOrUpdate(tag));
	}

	/**
	 * 标签表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(tagService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-tag")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入tag")
	public void exportTag(@Parameter(hidden = true) @RequestParam Map<String, Object> tag, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<TagEntity> queryWrapper = Condition.getQueryWrapper(tag, TagEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(Tag::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(TagEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<TagExcel> list = tagService.exportTag(queryWrapper);
		ExcelUtil.export(response, "标签表数据" + DateUtil.time(), "标签表数据表", list, TagExcel.class);
	}


	/**
	 * 获取所有标签
	 */
	@GetMapping("getAllTag")
	@Operation(summary = "获取所有标签", description = "获取所有标签")
	public R<List<TagVO>> getAllTag() {
		return R.data(tagService.getAllTag());
	}

	/**
	 * 得到当前标签信息
	 */
	@GetMapping("getOneTag")
	@Operation(summary = "得到当前标签信息", description = "得到当前标签信息")
	public R<TagEntity> getOneTag(String id) {
		return R.data(tagService.getById(id));
	}

	/**
	 * 根据标签id获取图片信息
	 */
	@GetMapping("getImgListByTagId/{page}/{limit}")
	@Operation(summary = "根据标签id获取图片信息", description = "根据标签id获取图片信息")
	public R<IPage<ImgDetailVO>> getImgListByTag(@PathVariable long page, @PathVariable long limit, String id, Integer type) {
		IPage<ImgDetailVO> pages = tagService.getImgListByTag(page, limit, id, type);
		return R.data(pages);
	}

	/**
	 * 添加标签
	 */
	@PostMapping("saveTag")
	@Operation(summary = "添加标签", description = "添加标签")
	public R saveTag(@RequestBody TagDTO tagDTO) {
		tagService.saveTag(tagDTO);
		return R.status(true);
	}

}
