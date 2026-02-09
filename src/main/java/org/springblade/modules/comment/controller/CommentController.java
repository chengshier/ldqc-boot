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
package org.springblade.modules.comment.controller;

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
import org.springblade.modules.comment.pojo.dto.CommentDTO;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.comment.pojo.entity.CommentEntity;
import org.springblade.modules.comment.pojo.vo.CommentVO;
import org.springblade.modules.comment.excel.CommentExcel;
import org.springblade.modules.comment.wrapper.CommentWrapper;
import org.springblade.modules.comment.service.ICommentService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;

import java.util.Map;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 评论表 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-comment/comment")
@Tag(name = "评论表", description = "评论表接口")
public class CommentController extends BladeController {

	private final ICommentService commentService;

	/**
	 * 评论表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description = "传入comment")
	public R<CommentVO> detail(CommentEntity comment) {
		CommentEntity detail = commentService.getOne(Condition.getQueryWrapper(comment));
		return R.data(CommentWrapper.build().entityVO(detail));
	}

	/**
	 * 评论表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description = "传入comment")
	public R<IPage<CommentVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> comment, Query query) {
		IPage<CommentEntity> pages = commentService.page(Condition.getPage(query), Condition.getQueryWrapper(comment, CommentEntity.class));
		return R.data(CommentWrapper.build().pageVO(pages));
	}

	/**
	 * 评论表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description = "传入comment")
	public R<IPage<CommentVO>> page(CommentVO comment, Query query) {
		IPage<CommentVO> pages = commentService.selectCommentPage(Condition.getPage(query), comment);
		return R.data(pages);
	}

	/**
	 * 评论表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description = "传入comment")
	public R save(@Valid @RequestBody CommentEntity comment) {
		return R.status(commentService.save(comment));
	}

	/**
	 * 评论表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description = "传入comment")
	public R update(@Valid @RequestBody CommentEntity comment) {
		return R.status(commentService.updateById(comment));
	}

	/**
	 * 评论表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description = "传入comment")
	public R submit(@Valid @RequestBody CommentEntity comment) {
		return R.status(commentService.saveOrUpdate(comment));
	}

	/**
	 * 评论表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(commentService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-comment")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description = "传入comment")
	public void exportComment(@Parameter(hidden = true) @RequestParam Map<String, Object> comment, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<CommentEntity> queryWrapper = Condition.getQueryWrapper(comment, CommentEntity.class);
//if (!AuthUtil.isAdministrator()) {
//queryWrapper.lambda().eq(Comment::getTenantId, bladeUser.getTenantId());
//}
//queryWrapper.lambda().eq(CommentEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<CommentExcel> list = commentService.exportComment(queryWrapper);
		ExcelUtil.export(response, "评论表数据" + DateUtil.time(), "评论表数据表", list, CommentExcel.class);
	}


	/**
	 * 得到当前图片下的所有一级评论
	 */
	@RequestMapping("/getAllOneCommentByImgId")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "得到当前图片下的所有一级评论", description = "传入page, limit, mid, uid")
	public R<IPage<CommentVO>> getAllOneCommentByImgId(Query query, @RequestParam String mid, @RequestParam String uid) {
		IPage<CommentVO> page = Condition.getPage(query);
		return R.data(commentService.getAllOneCommentByImgId(page, mid, uid));
	}

	/**
	 * 得到评论信息
	 */
	@RequestMapping("/getComment")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "得到评论信息", description = "传入id")
	public R<CommentEntity> getComment(@RequestParam String id) {
		return R.data(commentService.getById(id));
	}

	/**
	 * 增加一条评论
	 */
	@RequestMapping("/addComment")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "增加一条评论", description = "传入comment")
	public R<CommentVO> addComment(@RequestBody CommentDTO comment) {
		return R.data(commentService.addComment(comment));
	}

	/**
	 * 分页查询一级评论下的所有二级评论
	 */
	@RequestMapping("/getAllTwoCommentByOneId")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "分页查询一级评论下的所有二级评论", description = "传入page, limit, id, uid")
	public R<IPage<CommentVO>> getAllTwoCommentByOneId(Query query, @RequestParam String id, @RequestParam String uid) {
		IPage<CommentVO> page = Condition.getPage(query);
		return R.data(commentService.getAllTwoCommentByOneId(page, id, uid));
	}

	/**
	 * 得到当前一级评论下的所有二级评论 (列表)
	 */
	@RequestMapping("/getAllTwoComment")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "得到当前一级评论下的所有二级评论", description = "传入id, uid")
	public R<List<CommentVO>> getAllTwoComment(@RequestParam String id, @RequestParam String uid) {
		return R.data(commentService.getAllTwoComment(id, uid));
	}

	/**
	 * 查看所有回复的评论
	 */
	@RequestMapping("/getAllReplyComment")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "查看所有回复的评论", description = "传入page, limit, uid")
	public R<List<CommentVO>> getAllReplyComment(Query query, @RequestParam String uid) {
		IPage<CommentVO> page = Condition.getPage(query);
		return R.data(commentService.getAllReplyComment(page, uid));
	}

	/**
	 * 得到所有的一级评论并携带一个二级评论
	 */
	@RequestMapping("/getAllComment")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "得到所有的一级评论并携带一个二级评论", description = "传入page, limit, mid, uid")
	public R<IPage<CommentVO>> getAllComment(Query query, @RequestParam String mid, @RequestParam String uid) {
		IPage<CommentVO> page = Condition.getPage(query);
		return R.data(commentService.getAllComment(page, mid, uid));
	}

	/**
	 * 跳转评论
	 */
	@RequestMapping("/scrollComment")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "跳转评论", description = "传入id, mid, uid")
	public R<Map<String, Object>> scrollComment(@RequestParam String id, @RequestParam String mid, @RequestParam String uid) {
		return R.data(commentService.scrollComment(id, mid, uid));
	}

	/**
	 * 删除评论
	 */
	@RequestMapping("/delComment")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "删除评论", description = "传入id")
	public R<Void> delComment(@RequestParam String id) {
		commentService.delComment(id);
		return R.status(true);
	}

	/**
	 * 获取热门评论（按点赞数排序）
	 */
	@RequestMapping("/getAllTrendCommentByImage")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "获取热门评论", description = "传入page, limit, mid, uid")
	public R<IPage<CommentVO>> getAllTrendCommentByImage(Query query, @RequestParam String mid, @RequestParam String uid) {
		IPage<CommentVO> page = Condition.getPage(query);
		return R.data(commentService.getAllTrendCommentByImage(page, mid, uid));
	}

}
