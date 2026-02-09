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
package org.springblade.modules.album.controller;

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
import org.springblade.modules.album.pojo.dto.AlbumDTO;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.album.pojo.entity.AlbumEntity;
import org.springblade.modules.album.pojo.vo.AlbumVO;
import org.springblade.modules.album.excel.AlbumExcel;
import org.springblade.modules.album.wrapper.AlbumWrapper;
import org.springblade.modules.album.service.IAlbumService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 相册表 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-album/album")
@Tag(name = "相册表", description = "相册表接口")
public class AlbumController extends BladeController {

	private final IAlbumService albumService;

	/**
	 * 相册表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入album")
	public R<AlbumVO> detail(AlbumEntity album) {
		AlbumEntity detail = albumService.getOne(Condition.getQueryWrapper(album));
		return R.data(AlbumWrapper.build().entityVO(detail));
	}
	/**
	 * 相册表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入album")
	public R<IPage<AlbumVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> album, Query query) {
		IPage<AlbumEntity> pages = albumService.page(Condition.getPage(query), Condition.getQueryWrapper(album, AlbumEntity.class));
		return R.data(AlbumWrapper.build().pageVO(pages));
	}

	/**
	 * 相册表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入album")
	public R<IPage<AlbumVO>> page(AlbumVO album, Query query) {
		IPage<AlbumVO> pages = albumService.selectAlbumPage(Condition.getPage(query), album);
		return R.data(pages);
	}

	/**
	 * 相册表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入album")
	public R save(@Valid @RequestBody AlbumEntity album) {
		return R.status(albumService.save(album));
	}

	/**
	 * 相册表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入album")
	public R update(@Valid @RequestBody AlbumEntity album) {
		return R.status(albumService.updateById(album));
	}

	/**
	 * 相册表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入album")
	public R submit(@Valid @RequestBody AlbumEntity album) {
		return R.status(albumService.saveOrUpdate(album));
	}

	/**
	 * 相册表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(albumService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-album")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入album")
	public void exportAlbum(@Parameter(hidden = true) @RequestParam Map<String, Object> album, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<AlbumEntity> queryWrapper = Condition.getQueryWrapper(album, AlbumEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(Album::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(AlbumEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<AlbumExcel> list = albumService.exportAlbum(queryWrapper);
		ExcelUtil.export(response, "相册表数据" + DateUtil.time(), "相册表数据表", list, AlbumExcel.class);
	}


	@GetMapping("/getAllAlbum")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "得到当前用户的所有专辑", description = "传入uid")
	public R<List<AlbumVO>> getAllAlbum(Long uid) {
		return R.data(albumService.getAllAlbum(uid));
	}

	@PostMapping("/saveAlbum")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "保存专辑", description = "传入albumDTO")
	public R saveAlbum(@Valid @RequestBody AlbumDTO albumDTO) {
		albumService.saveAlbum(albumDTO);
		return R.success("保存成功");
	}

	@GetMapping("/getAlbum")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "得到专辑信息", description = "传入id")
	public R<AlbumVO> getAlbum(Long id) {
		return R.data(albumService.getAlbum(id));
	}

	@PostMapping("/deleteAlbum")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "删除专辑", description = "传入id和uid")
	public R deleteAlbum(@RequestParam Long id, @RequestParam Long uid) {
		albumService.deleteAlbum(id, uid);
		return R.success("删除成功");
	}

	@PostMapping("/updateAlbum")
	@ApiOperationSupport(order = 14)
	@Operation(summary = "更新专辑", description = "传入albumDTO")
	public R updateAlbum(@Valid @RequestBody AlbumDTO albumDTO) {
		albumService.updateAlbum(albumDTO);
		return R.success("更新成功");
	}

}
