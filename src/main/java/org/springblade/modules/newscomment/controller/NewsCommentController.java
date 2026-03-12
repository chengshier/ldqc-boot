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
package org.springblade.modules.newscomment.controller;

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
import org.springblade.modules.newscomment.pojo.entity.NewsCommentEntity;
import org.springblade.modules.newscomment.pojo.vo.NewsCommentVO;
import org.springblade.modules.newscomment.excel.NewsCommentExcel;
import org.springblade.modules.newscomment.wrapper.NewsCommentWrapper;
import org.springblade.modules.newscomment.service.INewsCommentService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 新闻评论表 控制器
 *
 * @author BladeX
 * @since 2026-03-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-newscomment/newsComment")
@Tag(name = "新闻评论表", description = "新闻评论表接口")
public class NewsCommentController extends BladeController {

	private final INewsCommentService newsCommentService;

	/**
	 * 新闻评论表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入newsComment")
	public R<NewsCommentVO> detail(NewsCommentEntity newsComment) {
		NewsCommentEntity detail = newsCommentService.getOne(Condition.getQueryWrapper(newsComment));
		return R.data(NewsCommentWrapper.build().entityVO(detail));
	}
	/**
	 * 新闻评论表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入newsComment")
	public R<IPage<NewsCommentVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> newsComment, Query query) {
		IPage<NewsCommentEntity> pages = newsCommentService.page(Condition.getPage(query), Condition.getQueryWrapper(newsComment, NewsCommentEntity.class));
		return R.data(NewsCommentWrapper.build().pageVO(pages));
	}

	/**
	 * 新闻评论表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入newsComment")
	public R<IPage<NewsCommentVO>> page(NewsCommentVO newsComment, Query query) {
		IPage<NewsCommentVO> pages = newsCommentService.selectNewsCommentPage(Condition.getPage(query), newsComment);
		return R.data(pages);
	}

	/**
	 * 新闻评论表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入newsComment")
	public R save(@Valid @RequestBody NewsCommentEntity newsComment) {
		return R.status(newsCommentService.save(newsComment));
	}

	/**
	 * 新闻评论表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入newsComment")
	public R update(@Valid @RequestBody NewsCommentEntity newsComment) {
		return R.status(newsCommentService.updateById(newsComment));
	}

	/**
	 * 新闻评论表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入newsComment")
	public R submit(@Valid @RequestBody NewsCommentEntity newsComment) {
		return R.status(newsCommentService.saveOrUpdate(newsComment));
	}

	/**
	 * 新闻评论表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(newsCommentService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-newsComment")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入newsComment")
	public void exportNewsComment(@Parameter(hidden = true) @RequestParam Map<String, Object> newsComment, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<NewsCommentEntity> queryWrapper = Condition.getQueryWrapper(newsComment, NewsCommentEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(NewsComment::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(NewsCommentEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<NewsCommentExcel> list = newsCommentService.exportNewsComment(queryWrapper);
		ExcelUtil.export(response, "新闻评论表数据" + DateUtil.time(), "新闻评论表数据表", list, NewsCommentExcel.class);
	}

}
