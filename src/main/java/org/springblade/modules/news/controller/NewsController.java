package org.springblade.modules.news.controller;

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
import org.springblade.modules.news.excel.NewsExcel;
import org.springblade.modules.news.pojo.entity.NewsEntity;
import org.springblade.modules.news.pojo.vo.NewsVO;
import org.springblade.modules.news.service.INewsService;
import org.springblade.modules.news.wrapper.NewsWrapper;
import org.springblade.modules.newscomment.pojo.entity.NewsCommentEntity;
import org.springblade.modules.newscomment.service.INewsCommentService;
import org.springblade.modules.newsimages.pojo.entity.NewsImagesEntity;
import org.springblade.modules.newsimages.service.INewsImagesService;
import org.springblade.modules.newsuseraction.pojo.entity.NewsUserActionEntity;
import org.springblade.modules.newsuseraction.service.INewsUserActionService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("blade-news/news")
@Tag(name = "News", description = "News API")
public class NewsController extends BladeController {

    private final INewsService newsService;
    private final INewsImagesService newsImagesService;
    private final INewsCommentService newsCommentService;
    private final INewsUserActionService newsUserActionService;

    @GetMapping("/detail")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "Detail", description = "news detail")
    public R<NewsVO> detail(NewsEntity news) {
        NewsEntity detail = newsService.getOne(Condition.getQueryWrapper(news));
        return R.data(NewsWrapper.build().entityVO(detail));
    }

    @GetMapping("/list")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "List", description = "news list")
    public R<IPage<NewsVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> news, Query query) {
        IPage<NewsEntity> pages = newsService.page(Condition.getPage(query), Condition.getQueryWrapper(news, NewsEntity.class));
        return R.data(NewsWrapper.build().pageVO(pages));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "Page", description = "news page")
    public R<IPage<NewsVO>> page(NewsVO news, Query query) {
        IPage<NewsVO> pages = newsService.selectNewsPage(Condition.getPage(query), news);
        return R.data(pages);
    }

    @PostMapping("/save")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "Save", description = "save news")
    public R save(@Valid @RequestBody NewsEntity news) {
        return R.status(newsService.save(news));
    }

    @PostMapping("/update")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "Update", description = "update news")
    public R update(@Valid @RequestBody NewsEntity news) {
        return R.status(newsService.updateById(news));
    }

    @PostMapping("/submit")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "Submit", description = "save or update news")
    public R submit(@Valid @RequestBody NewsEntity news) {
        return R.status(newsService.saveOrUpdate(news));
    }

    @PostMapping("/remove")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "Remove", description = "remove news")
    public R remove(@Parameter(description = "ids", required = true) @RequestParam String ids) {
        return R.status(newsService.deleteLogic(Func.toLongList(ids)));
    }

    @IsAdmin
    @GetMapping("/export-news")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "Export", description = "export news")
    public void exportNews(@Parameter(hidden = true) @RequestParam Map<String, Object> news, BladeUser bladeUser, HttpServletResponse response) {
        QueryWrapper<NewsEntity> queryWrapper = Condition.getQueryWrapper(news, NewsEntity.class);
        List<NewsExcel> list = newsService.exportNews(queryWrapper);
        ExcelUtil.export(response, "news_" + DateUtil.time(), "news", list, NewsExcel.class);
    }

    @GetMapping("/mobile/page")
    @ApiOperationSupport(order = 20)
    @Operation(summary = "Mobile page", description = "mobile news page")
    public R<IPage<NewsVO>> mobilePage(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) Long categoryId) {
        Query query = new Query();
        query.setCurrent(current);
        query.setSize(size);

        NewsVO news = new NewsVO();
        news.setCategoryId(categoryId);
        news.setNewsStatus(1);

        IPage<NewsVO> pages = newsService.selectNewsPage(Condition.getPage(query), news);
        return R.data(pages);
    }

    @GetMapping("/mobile/top")
    @ApiOperationSupport(order = 21)
    @Operation(summary = "Mobile top news")
    public R<NewsEntity> getTopNews() {
        NewsEntity topNews = newsService.getTopNews();
        if (topNews != null) {
            topNews.setImages(getNewsImageUrls(topNews.getId(), topNews.getCover()));
        }
        return R.data(topNews);
    }

    @GetMapping("/mobile/detail")
    @ApiOperationSupport(order = 22)
    @Operation(summary = "Mobile news detail", description = "mobile detail")
    public R<NewsEntity> getNewsDetail(@RequestParam Long id) {
        NewsEntity detail = newsService.getNewsDetail(id);
        if (detail == null) {
            return R.fail("news not found");
        }
        detail.setImages(getNewsImageUrls(detail.getId(), detail.getCover()));
        return R.data(detail);
    }

    @GetMapping("/mobile/detail-full")
    @ApiOperationSupport(order = 23)
    @Operation(summary = "Mobile news aggregate detail", description = "news detail aggregate for mini-program")
    public R<Map<String, Object>> getNewsDetailFull(@RequestParam Long id,
                                                    @RequestParam(required = false) Long userId) {
        NewsEntity detail = newsService.getNewsDetail(id);
        if (detail == null) {
            return R.fail("news not found");
        }

        List<String> images = getNewsImageUrls(id, detail.getCover());
        detail.setImages(images);

        List<NewsCommentEntity> comments = newsCommentService.page(
            new Page<>(1, 20),
            new QueryWrapper<NewsCommentEntity>()
                .eq("news_id", id)
                .eq("comment_status", 1)
                .eq("is_deleted", 0)
                .orderByDesc("create_time")
        ).getRecords();

        List<NewsEntity> related = newsService.page(
            new Page<>(1, 6),
            new QueryWrapper<NewsEntity>()
                .eq("is_deleted", 0)
                .eq("news_status", 1)
                .eq(detail.getCategoryId() != null, "category_id", detail.getCategoryId())
                .ne("id", id)
                .orderByDesc("is_top")
                .orderByDesc("publish_time")
                .orderByDesc("create_time")
        ).getRecords();
        related.forEach(item -> item.setImages(getNewsImageUrls(item.getId(), item.getCover())));

        Map<String, Boolean> action = new LinkedHashMap<>();
        action.put("liked", false);
        action.put("collected", false);
        action.put("shared", false);
        if (userId != null) {
            List<NewsUserActionEntity> actions = newsUserActionService.list(
                new QueryWrapper<NewsUserActionEntity>()
                    .eq("news_id", id)
                    .eq("user_id", userId)
                    .eq("is_deleted", 0)
            );
            action.put("liked", hasAction(actions, 1));
            action.put("collected", hasAction(actions, 2));
            action.put("shared", hasAction(actions, 3));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("news", detail);
        result.put("images", images);
        result.put("comments", comments);
        result.put("related", related);
        result.put("action", action);
        return R.data(result);
    }

    private boolean hasAction(List<NewsUserActionEntity> actions, int actionType) {
        return actions != null && actions.stream().anyMatch(item -> item.getActionType() != null && item.getActionType() == actionType);
    }

    private List<String> getNewsImageUrls(Long newsId, String cover) {
        List<String> imageUrls = newsImagesService.list(
            new QueryWrapper<NewsImagesEntity>()
                .eq("news_id", newsId)
                .eq("is_deleted", 0)
                .orderByAsc("sort_order")
        ).stream().map(NewsImagesEntity::getImageUrl).filter(Func::isNotBlank).collect(Collectors.toList());
        if (imageUrls.isEmpty() && Func.isNotBlank(cover)) {
            return Collections.singletonList(cover);
        }
        return imageUrls;
    }
}
