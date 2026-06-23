package org.springblade.modules.newscomment.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.news.pojo.entity.NewsEntity;
import org.springblade.modules.news.service.INewsService;
import org.springblade.modules.newscomment.excel.NewsCommentExcel;
import org.springblade.modules.newscomment.pojo.entity.NewsCommentEntity;
import org.springblade.modules.newscomment.pojo.vo.NewsCommentVO;
import org.springblade.modules.newscomment.service.INewsCommentService;
import org.springblade.modules.newscomment.wrapper.NewsCommentWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("blade-newscomment/newsComment")
@Tag(name = "NewsComment", description = "News comment API")
public class NewsCommentController extends BladeController {

	private final INewsCommentService newsCommentService;
	private final INewsService newsService;

	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "Detail", description = "news comment detail")
	public R<NewsCommentVO> detail(NewsCommentEntity newsComment) {
		NewsCommentEntity detail = newsCommentService.getOne(Condition.getQueryWrapper(newsComment));
		return R.data(NewsCommentWrapper.build().entityVO(detail));
	}

	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "List", description = "news comment list")
	public R<IPage<NewsCommentVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> newsComment, Query query) {
		IPage<NewsCommentEntity> pages = newsCommentService.page(Condition.getPage(query), Condition.getQueryWrapper(newsComment, NewsCommentEntity.class));
		return R.data(NewsCommentWrapper.build().pageVO(pages));
	}

	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "Page", description = "news comment page")
	public R<IPage<NewsCommentVO>> page(NewsCommentVO newsComment, Query query) {
		IPage<NewsCommentVO> pages = newsCommentService.selectNewsCommentPage(Condition.getPage(query), newsComment);
		return R.data(pages);
	}

	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "Save", description = "save news comment")
	public R save(@Valid @RequestBody NewsCommentEntity newsComment) {
		return R.status(newsCommentService.save(newsComment));
	}

	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "Update", description = "update news comment")
	public R update(@Valid @RequestBody NewsCommentEntity newsComment) {
		return R.status(newsCommentService.updateById(newsComment));
	}

	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "Submit", description = "save or update news comment")
	public R submit(@Valid @RequestBody NewsCommentEntity newsComment) {
		return R.status(newsCommentService.saveOrUpdate(newsComment));
	}

	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "Remove", description = "remove news comment")
	public R remove(@Parameter(description = "ids", required = true) @RequestParam String ids) {
		return R.status(newsCommentService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-newsComment")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "Export", description = "export news comment")
	public void exportNewsComment(@Parameter(hidden = true) @RequestParam Map<String, Object> newsComment, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<NewsCommentEntity> queryWrapper = Condition.getQueryWrapper(newsComment, NewsCommentEntity.class);
		List<NewsCommentExcel> list = newsCommentService.exportNewsComment(queryWrapper);
		ExcelUtil.export(response, "news_comment_" + DateUtil.time(), "news_comment", list, NewsCommentExcel.class);
	}

	@GetMapping("/mobile/page")
	@ApiOperationSupport(order = 20)
	@Operation(summary = "Mobile page", description = "mobile news comment page")
	public R<IPage<NewsCommentEntity>> mobilePage(@RequestParam Long newsId,
										@RequestParam(defaultValue = "1") Integer current,
										@RequestParam(defaultValue = "20") Integer size) {
		IPage<NewsCommentEntity> pages = newsCommentService.page(
			new Page<>(current, size),
			new QueryWrapper<NewsCommentEntity>()
				.eq("news_id", newsId)
				.eq("comment_status", 1)
				.eq("is_deleted", 0)
				.orderByDesc("create_time")
		);
		return R.data(pages);
	}

	@PostMapping("/mobile/save")
	@ApiOperationSupport(order = 21)
	@Operation(summary = "Mobile save", description = "save mobile news comment")
	public R<Map<String, Object>> mobileSave(@RequestBody NewsCommentEntity newsComment) {
		if (newsComment.getNewsId() == null) {
			return R.fail("newsId is required");
		}
		if (Func.isBlank(newsComment.getContent())) {
			return R.fail("content is required");
		}

		newsComment.setContent(newsComment.getContent().trim());
		newsComment.setParentId(newsComment.getParentId() == null ? 0L : newsComment.getParentId());
		newsComment.setLikeCount(newsComment.getLikeCount() == null ? 0 : newsComment.getLikeCount());
		newsComment.setCommentStatus((byte) 1);

		boolean saved = newsCommentService.save(newsComment);
		if (!saved) {
			return R.fail("comment save failed");
		}

		NewsEntity news = newsService.getById(newsComment.getNewsId());
		if (news != null) {
			news.setCommentCount((news.getCommentCount() == null ? 0 : news.getCommentCount()) + 1);
			newsService.updateById(news);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("comment", newsComment);
		result.put("commentCount", news != null ? news.getCommentCount() : null);
		return R.data(result);
	}
}
