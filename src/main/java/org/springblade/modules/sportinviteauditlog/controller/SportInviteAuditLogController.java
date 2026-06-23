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
package org.springblade.modules.sportinviteauditlog.controller;

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
import org.springblade.modules.sportinviteauditlog.pojo.entity.SportInviteAuditLogEntity;
import org.springblade.modules.sportinviteauditlog.pojo.vo.SportInviteAuditLogVO;
import org.springblade.modules.sportinviteauditlog.excel.SportInviteAuditLogExcel;
import org.springblade.modules.sportinviteauditlog.wrapper.SportInviteAuditLogWrapper;
import org.springblade.modules.sportinviteauditlog.service.ISportInviteAuditLogService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 运动邀约审核日志表 控制器
 *
 * @author BladeX
 * @since 2026-05-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-sportinviteauditlog/sportInviteAuditLog")
@Tag(name = "运动邀约审核日志表", description = "运动邀约审核日志表接口")
public class SportInviteAuditLogController extends BladeController {

	private final ISportInviteAuditLogService sportInviteAuditLogService;

	/**
	 * 运动邀约审核日志表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入sportInviteAuditLog")
	public R<SportInviteAuditLogVO> detail(SportInviteAuditLogEntity sportInviteAuditLog) {
		SportInviteAuditLogEntity detail = sportInviteAuditLogService.getOne(Condition.getQueryWrapper(sportInviteAuditLog));
		return R.data(SportInviteAuditLogWrapper.build().entityVO(detail));
	}
	/**
	 * 运动邀约审核日志表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入sportInviteAuditLog")
	public R<IPage<SportInviteAuditLogVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> sportInviteAuditLog, Query query) {
		IPage<SportInviteAuditLogEntity> pages = sportInviteAuditLogService.page(Condition.getPage(query), Condition.getQueryWrapper(sportInviteAuditLog, SportInviteAuditLogEntity.class));
		return R.data(SportInviteAuditLogWrapper.build().pageVO(pages));
	}

	/**
	 * 运动邀约审核日志表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入sportInviteAuditLog")
	public R<IPage<SportInviteAuditLogVO>> page(SportInviteAuditLogVO sportInviteAuditLog, Query query) {
		IPage<SportInviteAuditLogVO> pages = sportInviteAuditLogService.selectSportInviteAuditLogPage(Condition.getPage(query), sportInviteAuditLog);
		return R.data(pages);
	}

	/**
	 * 运动邀约审核日志表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入sportInviteAuditLog")
	public R save(@Valid @RequestBody SportInviteAuditLogEntity sportInviteAuditLog) {
		return R.status(sportInviteAuditLogService.save(sportInviteAuditLog));
	}

	/**
	 * 运动邀约审核日志表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入sportInviteAuditLog")
	public R update(@Valid @RequestBody SportInviteAuditLogEntity sportInviteAuditLog) {
		return R.status(sportInviteAuditLogService.updateById(sportInviteAuditLog));
	}

	/**
	 * 运动邀约审核日志表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入sportInviteAuditLog")
	public R submit(@Valid @RequestBody SportInviteAuditLogEntity sportInviteAuditLog) {
		return R.status(sportInviteAuditLogService.saveOrUpdate(sportInviteAuditLog));
	}

	/**
	 * 运动邀约审核日志表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(sportInviteAuditLogService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-sportInviteAuditLog")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入sportInviteAuditLog")
	public void exportSportInviteAuditLog(@Parameter(hidden = true) @RequestParam Map<String, Object> sportInviteAuditLog, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<SportInviteAuditLogEntity> queryWrapper = Condition.getQueryWrapper(sportInviteAuditLog, SportInviteAuditLogEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(SportInviteAuditLog::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(SportInviteAuditLogEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<SportInviteAuditLogExcel> list = sportInviteAuditLogService.exportSportInviteAuditLog(queryWrapper);
		ExcelUtil.export(response, "运动邀约审核日志表数据" + DateUtil.time(), "运动邀约审核日志表数据表", list, SportInviteAuditLogExcel.class);
	}

}
