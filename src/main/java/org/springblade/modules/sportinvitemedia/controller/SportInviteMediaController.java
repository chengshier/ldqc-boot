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
package org.springblade.modules.sportinvitemedia.controller;

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
import org.springblade.modules.sportinvitemedia.pojo.entity.SportInviteMediaEntity;
import org.springblade.modules.sportinvitemedia.pojo.vo.SportInviteMediaVO;
import org.springblade.modules.sportinvitemedia.excel.SportInviteMediaExcel;
import org.springblade.modules.sportinvitemedia.wrapper.SportInviteMediaWrapper;
import org.springblade.modules.sportinvitemedia.service.ISportInviteMediaService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 运动邀约媒体表 控制器
 *
 * @author BladeX
 * @since 2026-05-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-sportinvitemedia/sportInviteMedia")
@Tag(name = "运动邀约媒体表", description = "运动邀约媒体表接口")
public class SportInviteMediaController extends BladeController {

	private final ISportInviteMediaService sportInviteMediaService;

	/**
	 * 运动邀约媒体表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入sportInviteMedia")
	public R<SportInviteMediaVO> detail(SportInviteMediaEntity sportInviteMedia) {
		SportInviteMediaEntity detail = sportInviteMediaService.getOne(Condition.getQueryWrapper(sportInviteMedia));
		return R.data(SportInviteMediaWrapper.build().entityVO(detail));
	}
	/**
	 * 运动邀约媒体表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入sportInviteMedia")
	public R<IPage<SportInviteMediaVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> sportInviteMedia, Query query) {
		IPage<SportInviteMediaEntity> pages = sportInviteMediaService.page(Condition.getPage(query), Condition.getQueryWrapper(sportInviteMedia, SportInviteMediaEntity.class));
		return R.data(SportInviteMediaWrapper.build().pageVO(pages));
	}

	/**
	 * 运动邀约媒体表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入sportInviteMedia")
	public R<IPage<SportInviteMediaVO>> page(SportInviteMediaVO sportInviteMedia, Query query) {
		IPage<SportInviteMediaVO> pages = sportInviteMediaService.selectSportInviteMediaPage(Condition.getPage(query), sportInviteMedia);
		return R.data(pages);
	}

	/**
	 * 运动邀约媒体表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入sportInviteMedia")
	public R save(@Valid @RequestBody SportInviteMediaEntity sportInviteMedia) {
		return R.status(sportInviteMediaService.save(sportInviteMedia));
	}

	/**
	 * 运动邀约媒体表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入sportInviteMedia")
	public R update(@Valid @RequestBody SportInviteMediaEntity sportInviteMedia) {
		return R.status(sportInviteMediaService.updateById(sportInviteMedia));
	}

	/**
	 * 运动邀约媒体表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入sportInviteMedia")
	public R submit(@Valid @RequestBody SportInviteMediaEntity sportInviteMedia) {
		return R.status(sportInviteMediaService.saveOrUpdate(sportInviteMedia));
	}

	/**
	 * 运动邀约媒体表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(sportInviteMediaService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-sportInviteMedia")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入sportInviteMedia")
	public void exportSportInviteMedia(@Parameter(hidden = true) @RequestParam Map<String, Object> sportInviteMedia, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<SportInviteMediaEntity> queryWrapper = Condition.getQueryWrapper(sportInviteMedia, SportInviteMediaEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(SportInviteMedia::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(SportInviteMediaEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<SportInviteMediaExcel> list = sportInviteMediaService.exportSportInviteMedia(queryWrapper);
		ExcelUtil.export(response, "运动邀约媒体表数据" + DateUtil.time(), "运动邀约媒体表数据表", list, SportInviteMediaExcel.class);
	}

}
