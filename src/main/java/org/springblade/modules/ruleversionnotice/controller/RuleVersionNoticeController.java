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
package org.springblade.modules.ruleversionnotice.controller;

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
import org.springblade.modules.ruleversionnotice.pojo.entity.RuleVersionNoticeEntity;
import org.springblade.modules.ruleversionnotice.pojo.vo.RuleVersionNoticeVO;
import org.springblade.modules.ruleversionnotice.excel.RuleVersionNoticeExcel;
import org.springblade.modules.ruleversionnotice.wrapper.RuleVersionNoticeWrapper;
import org.springblade.modules.ruleversionnotice.service.IRuleVersionNoticeService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 规则公示 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-ruleversionnotice/ruleVersionNotice")
@Tag(name = "规则公示", description = "规则公示接口")
public class RuleVersionNoticeController extends BladeController {

	private final IRuleVersionNoticeService ruleVersionNoticeService;

	/**
	 * 规则公示 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入ruleVersionNotice")
	public R<RuleVersionNoticeVO> detail(RuleVersionNoticeEntity ruleVersionNotice) {
		RuleVersionNoticeEntity detail = ruleVersionNoticeService.getOne(Condition.getQueryWrapper(ruleVersionNotice));
		return R.data(RuleVersionNoticeWrapper.build().entityVO(detail));
	}
	/**
	 * 规则公示 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入ruleVersionNotice")
	public R<IPage<RuleVersionNoticeVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> ruleVersionNotice, Query query) {
		IPage<RuleVersionNoticeEntity> pages = ruleVersionNoticeService.page(Condition.getPage(query), Condition.getQueryWrapper(ruleVersionNotice, RuleVersionNoticeEntity.class));
		return R.data(RuleVersionNoticeWrapper.build().pageVO(pages));
	}

	/**
	 * 规则公示 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入ruleVersionNotice")
	public R<IPage<RuleVersionNoticeVO>> page(RuleVersionNoticeVO ruleVersionNotice, Query query) {
		IPage<RuleVersionNoticeVO> pages = ruleVersionNoticeService.selectRuleVersionNoticePage(Condition.getPage(query), ruleVersionNotice);
		return R.data(pages);
	}

	/**
	 * 规则公示 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入ruleVersionNotice")
	public R save(@Valid @RequestBody RuleVersionNoticeEntity ruleVersionNotice) {
		return R.status(ruleVersionNoticeService.save(ruleVersionNotice));
	}

	/**
	 * 规则公示 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入ruleVersionNotice")
	public R update(@Valid @RequestBody RuleVersionNoticeEntity ruleVersionNotice) {
		return R.status(ruleVersionNoticeService.updateById(ruleVersionNotice));
	}

	/**
	 * 规则公示 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入ruleVersionNotice")
	public R submit(@Valid @RequestBody RuleVersionNoticeEntity ruleVersionNotice) {
		return R.status(ruleVersionNoticeService.saveOrUpdate(ruleVersionNotice));
	}

	/**
	 * 规则公示 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(ruleVersionNoticeService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-ruleVersionNotice")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入ruleVersionNotice")
	public void exportRuleVersionNotice(@Parameter(hidden = true) @RequestParam Map<String, Object> ruleVersionNotice, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<RuleVersionNoticeEntity> queryWrapper = Condition.getQueryWrapper(ruleVersionNotice, RuleVersionNoticeEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(RuleVersionNotice::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(RuleVersionNoticeEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<RuleVersionNoticeExcel> list = ruleVersionNoticeService.exportRuleVersionNotice(queryWrapper);
		ExcelUtil.export(response, "规则公示数据" + DateUtil.time(), "规则公示数据表", list, RuleVersionNoticeExcel.class);
	}

}


