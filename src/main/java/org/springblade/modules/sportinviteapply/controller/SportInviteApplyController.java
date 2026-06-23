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
package org.springblade.modules.sportinviteapply.controller;

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
import org.springblade.modules.sportinviteapply.pojo.entity.SportInviteApplyEntity;
import org.springblade.modules.sportinviteapply.pojo.vo.SportInviteApplyVO;
import org.springblade.modules.sportinviteapply.excel.SportInviteApplyExcel;
import org.springblade.modules.sportinviteapply.wrapper.SportInviteApplyWrapper;
import org.springblade.modules.sportinviteapply.service.ISportInviteApplyService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 运动邀约申请表 控制器
 *
 * @author BladeX
 * @since 2026-05-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-sportinviteapply/sportInviteApply")
@Tag(name = "运动邀约申请表", description = "运动邀约申请表接口")
public class SportInviteApplyController extends BladeController {

	private final ISportInviteApplyService sportInviteApplyService;

	/**
	 * 运动邀约申请表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入sportInviteApply")
	public R<SportInviteApplyVO> detail(SportInviteApplyEntity sportInviteApply) {
		SportInviteApplyEntity detail = sportInviteApplyService.getOne(Condition.getQueryWrapper(sportInviteApply));
		return R.data(SportInviteApplyWrapper.build().entityVO(detail));
	}
	/**
	 * 运动邀约申请表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入sportInviteApply")
	public R<IPage<SportInviteApplyVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> sportInviteApply, Query query) {
		IPage<SportInviteApplyEntity> pages = sportInviteApplyService.page(Condition.getPage(query), Condition.getQueryWrapper(sportInviteApply, SportInviteApplyEntity.class));
		return R.data(SportInviteApplyWrapper.build().pageVO(pages));
	}

	/**
	 * 运动邀约申请表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入sportInviteApply")
	public R<IPage<SportInviteApplyVO>> page(SportInviteApplyVO sportInviteApply, Query query) {
		IPage<SportInviteApplyVO> pages = sportInviteApplyService.selectSportInviteApplyPage(Condition.getPage(query), sportInviteApply);
		return R.data(pages);
	}

	/**
	 * 运动邀约申请表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入sportInviteApply")
	public R save(@Valid @RequestBody SportInviteApplyEntity sportInviteApply) {
		return R.status(sportInviteApplyService.save(sportInviteApply));
	}

	/**
	 * 运动邀约申请表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入sportInviteApply")
	public R update(@Valid @RequestBody SportInviteApplyEntity sportInviteApply) {
		return R.status(sportInviteApplyService.updateById(sportInviteApply));
	}

	/**
	 * 运动邀约申请表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入sportInviteApply")
	public R submit(@Valid @RequestBody SportInviteApplyEntity sportInviteApply) {
		return R.status(sportInviteApplyService.saveOrUpdate(sportInviteApply));
	}

	/**
	 * 运动邀约申请表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(sportInviteApplyService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-sportInviteApply")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入sportInviteApply")
	public void exportSportInviteApply(@Parameter(hidden = true) @RequestParam Map<String, Object> sportInviteApply, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<SportInviteApplyEntity> queryWrapper = Condition.getQueryWrapper(sportInviteApply, SportInviteApplyEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(SportInviteApply::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(SportInviteApplyEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<SportInviteApplyExcel> list = sportInviteApplyService.exportSportInviteApply(queryWrapper);
		ExcelUtil.export(response, "运动邀约申请表数据" + DateUtil.time(), "运动邀约申请表数据表", list, SportInviteApplyExcel.class);
	}

}
