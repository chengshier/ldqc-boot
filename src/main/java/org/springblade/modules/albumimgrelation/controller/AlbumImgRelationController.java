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
package org.springblade.modules.albumimgrelation.controller;

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
import org.springblade.modules.albumimgrelation.pojo.entity.AlbumImgRelationEntity;
import org.springblade.modules.albumimgrelation.pojo.vo.AlbumImgRelationVO;
import org.springblade.modules.albumimgrelation.excel.AlbumImgRelationExcel;
import org.springblade.modules.albumimgrelation.wrapper.AlbumImgRelationWrapper;
import org.springblade.modules.albumimgrelation.service.IAlbumImgRelationService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 相册图片关系表 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-albumimgrelation/albumImgRelation")
@Tag(name = "相册图片关系表", description = "相册图片关系表接口")
public class AlbumImgRelationController extends BladeController {

	private final IAlbumImgRelationService albumImgRelationService;

	/**
	 * 相册图片关系表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入albumImgRelation")
	public R<AlbumImgRelationVO> detail(AlbumImgRelationEntity albumImgRelation) {
		AlbumImgRelationEntity detail = albumImgRelationService.getOne(Condition.getQueryWrapper(albumImgRelation));
		return R.data(AlbumImgRelationWrapper.build().entityVO(detail));
	}
	/**
	 * 相册图片关系表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入albumImgRelation")
	public R<IPage<AlbumImgRelationVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> albumImgRelation, Query query) {
		IPage<AlbumImgRelationEntity> pages = albumImgRelationService.page(Condition.getPage(query), Condition.getQueryWrapper(albumImgRelation, AlbumImgRelationEntity.class));
		return R.data(AlbumImgRelationWrapper.build().pageVO(pages));
	}

	/**
	 * 相册图片关系表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入albumImgRelation")
	public R<IPage<AlbumImgRelationVO>> page(AlbumImgRelationVO albumImgRelation, Query query) {
		IPage<AlbumImgRelationVO> pages = albumImgRelationService.selectAlbumImgRelationPage(Condition.getPage(query), albumImgRelation);
		return R.data(pages);
	}

	/**
	 * 相册图片关系表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入albumImgRelation")
	public R save(@Valid @RequestBody AlbumImgRelationEntity albumImgRelation) {
		return R.status(albumImgRelationService.save(albumImgRelation));
	}

	/**
	 * 相册图片关系表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入albumImgRelation")
	public R update(@Valid @RequestBody AlbumImgRelationEntity albumImgRelation) {
		return R.status(albumImgRelationService.updateById(albumImgRelation));
	}

	/**
	 * 相册图片关系表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入albumImgRelation")
	public R submit(@Valid @RequestBody AlbumImgRelationEntity albumImgRelation) {
		return R.status(albumImgRelationService.saveOrUpdate(albumImgRelation));
	}

	/**
	 * 相册图片关系表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(albumImgRelationService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-albumImgRelation")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入albumImgRelation")
	public void exportAlbumImgRelation(@Parameter(hidden = true) @RequestParam Map<String, Object> albumImgRelation, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<AlbumImgRelationEntity> queryWrapper = Condition.getQueryWrapper(albumImgRelation, AlbumImgRelationEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(AlbumImgRelation::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(AlbumImgRelationEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<AlbumImgRelationExcel> list = albumImgRelationService.exportAlbumImgRelation(queryWrapper);
		ExcelUtil.export(response, "相册图片关系表数据" + DateUtil.time(), "相册图片关系表数据表", list, AlbumImgRelationExcel.class);
	}



	/**
	 * 保存关联
	 */
//	@PostMapping("/save")
//	@ApiOperationSupport(order = 1)
//	@Operation(summary = "保存关联", description = "传入entity")
//	public R save(@RequestBody AlbumImgRelationEntity entity) {
//		return R.status(albumImgRelationService.save(entity));
//	}

}
