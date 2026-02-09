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
package org.springblade.modules.messageuserrelation.controller;

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
import org.springblade.modules.messageuserrelation.pojo.entity.MessageUserRelationEntity;
import org.springblade.modules.messageuserrelation.pojo.vo.MessageUserRelationVO;
import org.springblade.modules.messageuserrelation.excel.MessageUserRelationExcel;
import org.springblade.modules.messageuserrelation.wrapper.MessageUserRelationWrapper;
import org.springblade.modules.messageuserrelation.service.IMessageUserRelationService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 消息用户关系表 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-messageuserrelation/messageUserRelation")
@Tag(name = "消息用户关系表", description = "消息用户关系表接口")
public class MessageUserRelationController extends BladeController {

	private final IMessageUserRelationService messageUserRelationService;

	/**
	 * 消息用户关系表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入messageUserRelation")
	public R<MessageUserRelationVO> detail(MessageUserRelationEntity messageUserRelation) {
		MessageUserRelationEntity detail = messageUserRelationService.getOne(Condition.getQueryWrapper(messageUserRelation));
		return R.data(MessageUserRelationWrapper.build().entityVO(detail));
	}
	/**
	 * 消息用户关系表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入messageUserRelation")
	public R<IPage<MessageUserRelationVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> messageUserRelation, Query query) {
		IPage<MessageUserRelationEntity> pages = messageUserRelationService.page(Condition.getPage(query), Condition.getQueryWrapper(messageUserRelation, MessageUserRelationEntity.class));
		return R.data(MessageUserRelationWrapper.build().pageVO(pages));
	}

	/**
	 * 消息用户关系表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入messageUserRelation")
	public R<IPage<MessageUserRelationVO>> page(MessageUserRelationVO messageUserRelation, Query query) {
		IPage<MessageUserRelationVO> pages = messageUserRelationService.selectMessageUserRelationPage(Condition.getPage(query), messageUserRelation);
		return R.data(pages);
	}

	/**
	 * 消息用户关系表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入messageUserRelation")
	public R save(@Valid @RequestBody MessageUserRelationEntity messageUserRelation) {
		return R.status(messageUserRelationService.save(messageUserRelation));
	}

	/**
	 * 消息用户关系表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入messageUserRelation")
	public R update(@Valid @RequestBody MessageUserRelationEntity messageUserRelation) {
		return R.status(messageUserRelationService.updateById(messageUserRelation));
	}

	/**
	 * 消息用户关系表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入messageUserRelation")
	public R submit(@Valid @RequestBody MessageUserRelationEntity messageUserRelation) {
		return R.status(messageUserRelationService.saveOrUpdate(messageUserRelation));
	}

	/**
	 * 消息用户关系表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(messageUserRelationService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-messageUserRelation")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入messageUserRelation")
	public void exportMessageUserRelation(@Parameter(hidden = true) @RequestParam Map<String, Object> messageUserRelation, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<MessageUserRelationEntity> queryWrapper = Condition.getQueryWrapper(messageUserRelation, MessageUserRelationEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(MessageUserRelation::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(MessageUserRelationEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<MessageUserRelationExcel> list = messageUserRelationService.exportMessageUserRelation(queryWrapper);
		ExcelUtil.export(response, "消息用户关系表数据" + DateUtil.time(), "消息用户关系表数据表", list, MessageUserRelationExcel.class);
	}

}
