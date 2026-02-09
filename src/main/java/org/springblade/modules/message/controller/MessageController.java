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
package org.springblade.modules.message.controller;

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
import org.springblade.modules.message.pojo.dto.MessageDTO;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.message.pojo.entity.MessageEntity;
import org.springblade.modules.message.pojo.vo.MessageVO;
import org.springblade.modules.message.excel.MessageExcel;
import org.springblade.modules.message.wrapper.MessageWrapper;
import org.springblade.modules.message.service.IMessageService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 消息表 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-message/message")
@Tag(name = "消息表", description = "消息表接口")
public class MessageController extends BladeController {

	private final IMessageService messageService;

	/**
	 * 消息表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入message")
	public R<MessageVO> detail(MessageEntity message) {
		MessageEntity detail = messageService.getOne(Condition.getQueryWrapper(message));
		return R.data(MessageWrapper.build().entityVO(detail));
	}
	/**
	 * 消息表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入message")
	public R<IPage<MessageVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> message, Query query) {
		IPage<MessageEntity> pages = messageService.page(Condition.getPage(query), Condition.getQueryWrapper(message, MessageEntity.class));
		return R.data(MessageWrapper.build().pageVO(pages));
	}

	/**
	 * 消息表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入message")
	public R<IPage<MessageVO>> page(MessageVO message, Query query) {
		IPage<MessageVO> pages = messageService.selectMessagePage(Condition.getPage(query), message);
		return R.data(pages);
	}

	/**
	 * 消息表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入message")
	public R save(@Valid @RequestBody MessageEntity message) {
		return R.status(messageService.save(message));
	}

	/**
	 * 消息表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入message")
	public R update(@Valid @RequestBody MessageEntity message) {
		return R.status(messageService.updateById(message));
	}

	/**
	 * 消息表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入message")
	public R submit(@Valid @RequestBody MessageEntity message) {
		return R.status(messageService.saveOrUpdate(message));
	}

	/**
	 * 消息表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(messageService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-message")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入message")
	public void exportMessage(@Parameter(hidden = true) @RequestParam Map<String, Object> message, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<MessageEntity> queryWrapper = Condition.getQueryWrapper(message, MessageEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(Message::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(MessageEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<MessageExcel> list = messageService.exportMessage(queryWrapper);
		ExcelUtil.export(response, "消息表数据" + DateUtil.time(), "消息表数据表", list, MessageExcel.class);
	}



	/**
	 * 获取聊天记录
	 */
	@GetMapping("/getChatRecord")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "获取聊天记录", description = "传入page, limit, sendUid, acceptUid")
	public R<IPage<MessageVO>> getChatRecord(Query query, @RequestParam String sendUid, @RequestParam String acceptUid) {
		IPage<MessageVO> page = Condition.getPage(query);
		return R.data(messageService.getChatRecord(page, sendUid, acceptUid));
	}

	/**
	 * 增加聊天记录
	 */
	@PostMapping("/addChatRecord")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "增加聊天记录", description = "传入MessageDTO")
	public R<Void> addChatRecord(@RequestBody MessageDTO messageDTO) {
		messageService.addChatRecord(messageDTO);
		return R.status(true);
	}

	/**
	 * 得到聊天的用户列表
	 */
	@GetMapping("/getChatUserList")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "得到聊天的用户列表", description = "传入uid")
	public R<List<MessageVO>> getChatUserList(@RequestParam String uid) {
		return R.data(messageService.getChatUserList(uid));
	}

	/**
	 * 更新聊天记录状态(已读)
	 */
	@PostMapping("/updateRecordCount")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "更新聊天记录状态", description = "传入sendId, acceptId")
	public R<Void> updateRecordCount(@RequestParam String sendId, @RequestParam String acceptId) {
		messageService.updateRecordCount(sendId, acceptId);
		return R.status(true);
	}

	/**
	 * 删除聊天记录
	 */
	@PostMapping("/deleteRecord")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "删除聊天记录", description = "传入sendId, acceptId")
	public R<Void> deleteRecord(@RequestParam String sendId, @RequestParam String acceptId) {
		messageService.deleteRecord(sendId, acceptId);
		return R.status(true);
	}

}
