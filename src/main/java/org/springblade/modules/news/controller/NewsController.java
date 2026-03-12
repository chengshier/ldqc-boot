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
package org.springblade.modules.news.controller;

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
import org.springblade.modules.news.pojo.entity.NewsEntity;
import org.springblade.modules.news.pojo.vo.NewsVO;
import org.springblade.modules.news.excel.NewsExcel;
import org.springblade.modules.news.wrapper.NewsWrapper;
import org.springblade.modules.news.service.INewsService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 新闻表 控制器
 *
 * @author BladeX
 * @since 2026-03-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-news/news")
@Tag(name = "新闻表", description = "新闻表接口")
public class NewsController extends BladeController {

	private final INewsService newsService;

	/**
	 * 新闻表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入news")
	public R<NewsVO> detail(NewsEntity news) {
		NewsEntity detail = newsService.getOne(Condition.getQueryWrapper(news));
		return R.data(NewsWrapper.build().entityVO(detail));
	}
	/**
	 * 新闻表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入news")
	public R<IPage<NewsVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> news, Query query) {
		IPage<NewsEntity> pages = newsService.page(Condition.getPage(query), Condition.getQueryWrapper(news, NewsEntity.class));
		return R.data(NewsWrapper.build().pageVO(pages));
	}

	/**
	 * 新闻表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入news")
	public R<IPage<NewsVO>> page(NewsVO news, Query query) {
		IPage<NewsVO> pages = newsService.selectNewsPage(Condition.getPage(query), news);
		return R.data(pages);
	}

	/**
	 * 新闻表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入news")
	public R save(@Valid @RequestBody NewsEntity news) {
		return R.status(newsService.save(news));
	}

	/**
	 * 新闻表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入news")
	public R update(@Valid @RequestBody NewsEntity news) {
		return R.status(newsService.updateById(news));
	}

	/**
	 * 新闻表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入news")
	public R submit(@Valid @RequestBody NewsEntity news) {
		return R.status(newsService.saveOrUpdate(news));
	}

	/**
	 * 新闻表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(newsService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-news")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入news")
	public void exportNews(@Parameter(hidden = true) @RequestParam Map<String, Object> news, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<NewsEntity> queryWrapper = Condition.getQueryWrapper(news, NewsEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(News::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(NewsEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<NewsExcel> list = newsService.exportNews(queryWrapper);
		ExcelUtil.export(response, "新闻表数据" + DateUtil.time(), "新闻表数据表", list, NewsExcel.class);
	}


	/**
	 * ================== 移动端接口 ==================
	 */

	/**
	 * 移动端-分页查询新闻列表
	 */
	@GetMapping("/mobile/page")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "移动端-分页查询", description = "传入categoryId")
	public R<IPage<NewsVO>> mobilePage(@RequestParam(defaultValue = "1") Integer current,
									   @RequestParam(defaultValue = "10") Integer size,
									   @RequestParam(required = false) Long categoryId) {
		Query query = new Query();
		query.setCurrent(current);
		query.setSize(size);

		NewsVO news = new NewsVO();
		news.setCategoryId(categoryId);
		news.setNewsStatus(1); // 只查询已发布的

		IPage<NewsVO> pages = newsService.selectNewsPage(Condition.getPage(query), news);
		return R.data(pages);
	}

	/**
	 * 移动端-获取热度新闻TOP1
	 */
	@GetMapping("/mobile/top")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "移动端-获取热度新闻TOP1")
	public R<NewsEntity> getTopNews() {
		return R.data(newsService.getTopNews());
	}

	/**
	 * 移动端-获取新闻详情
	 */
	@GetMapping("/mobile/detail")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "移动端-获取新闻详情", description = "传入id")
	public R<NewsEntity> getNewsDetail(@RequestParam Long id) {
		NewsEntity detail = newsService.getNewsDetail(id);
		if (detail == null) {
			return R.fail("新闻不存在");
		}
		return R.data(detail);
	}


}
