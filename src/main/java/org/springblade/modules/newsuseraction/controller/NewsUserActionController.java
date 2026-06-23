package org.springblade.modules.newsuseraction.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import org.springblade.modules.newsuseraction.excel.NewsUserActionExcel;
import org.springblade.modules.newsuseraction.pojo.entity.NewsUserActionEntity;
import org.springblade.modules.newsuseraction.pojo.vo.NewsUserActionVO;
import org.springblade.modules.newsuseraction.service.INewsUserActionService;
import org.springblade.modules.newsuseraction.wrapper.NewsUserActionWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("blade-newsuseraction/newsUserAction")
@Tag(name = "NewsUserAction", description = "News user action API")
public class NewsUserActionController extends BladeController {

	private final INewsUserActionService newsUserActionService;
	private final INewsService newsService;

	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "Detail", description = "news user action detail")
	public R<NewsUserActionVO> detail(NewsUserActionEntity newsUserAction) {
		NewsUserActionEntity detail = newsUserActionService.getOne(Condition.getQueryWrapper(newsUserAction));
		return R.data(NewsUserActionWrapper.build().entityVO(detail));
	}

	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "List", description = "news user action list")
	public R<IPage<NewsUserActionVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> newsUserAction, Query query) {
		IPage<NewsUserActionEntity> pages = newsUserActionService.page(Condition.getPage(query), Condition.getQueryWrapper(newsUserAction, NewsUserActionEntity.class));
		return R.data(NewsUserActionWrapper.build().pageVO(pages));
	}

	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "Page", description = "news user action page")
	public R<IPage<NewsUserActionVO>> page(NewsUserActionVO newsUserAction, Query query) {
		IPage<NewsUserActionVO> pages = newsUserActionService.selectNewsUserActionPage(Condition.getPage(query), newsUserAction);
		return R.data(pages);
	}

	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "Save", description = "save news user action")
	public R save(@Valid @RequestBody NewsUserActionEntity newsUserAction) {
		return R.status(newsUserActionService.save(newsUserAction));
	}

	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "Update", description = "update news user action")
	public R update(@Valid @RequestBody NewsUserActionEntity newsUserAction) {
		return R.status(newsUserActionService.updateById(newsUserAction));
	}

	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "Submit", description = "save or update news user action")
	public R submit(@Valid @RequestBody NewsUserActionEntity newsUserAction) {
		return R.status(newsUserActionService.saveOrUpdate(newsUserAction));
	}

	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "Remove", description = "remove news user action")
	public R remove(@Parameter(description = "ids", required = true) @RequestParam String ids) {
		return R.status(newsUserActionService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-newsUserAction")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "Export", description = "export news user action")
	public void exportNewsUserAction(@Parameter(hidden = true) @RequestParam Map<String, Object> newsUserAction, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<NewsUserActionEntity> queryWrapper = Condition.getQueryWrapper(newsUserAction, NewsUserActionEntity.class);
		List<NewsUserActionExcel> list = newsUserActionService.exportNewsUserAction(queryWrapper);
		ExcelUtil.export(response, "news_user_action_" + DateUtil.time(), "news_user_action", list, NewsUserActionExcel.class);
	}

	@PostMapping("/mobile/toggle")
	@ApiOperationSupport(order = 20)
	@Operation(summary = "Mobile toggle", description = "toggle or record mobile news action")
	public R<Map<String, Object>> mobileToggle(@RequestBody NewsUserActionEntity newsUserAction) {
		if (newsUserAction.getNewsId() == null || newsUserAction.getUserId() == null || newsUserAction.getActionType() == null) {
			return R.fail("newsId, userId and actionType are required");
		}

		QueryWrapper<NewsUserActionEntity> queryWrapper = new QueryWrapper<NewsUserActionEntity>()
			.eq("news_id", newsUserAction.getNewsId())
			.eq("user_id", newsUserAction.getUserId())
			.eq("action_type", newsUserAction.getActionType())
			.eq("is_deleted", 0)
			.last("limit 1");

		NewsUserActionEntity existing = newsUserActionService.getOne(queryWrapper);
		boolean active;
		if (newsUserAction.getActionType() == 3) {
			active = true;
			if (existing == null) {
				newsUserActionService.save(newsUserAction);
				changeNewsCounter(newsUserAction.getNewsId(), newsUserAction.getActionType(), 1);
			}
		} else if (existing != null) {
			newsUserActionService.deleteLogic(List.of(existing.getId()));
			changeNewsCounter(newsUserAction.getNewsId(), newsUserAction.getActionType(), -1);
			active = false;
		} else {
			newsUserActionService.save(newsUserAction);
			changeNewsCounter(newsUserAction.getNewsId(), newsUserAction.getActionType(), 1);
			active = true;
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("active", active);
		result.put("actionType", newsUserAction.getActionType());
		result.put("count", getActionCount(newsUserAction.getNewsId(), newsUserAction.getActionType()));
		return R.data(result);
	}

	private void changeNewsCounter(Long newsId, Byte actionType, int delta) {
		NewsEntity news = newsService.getById(newsId);
		if (news == null) {
			return;
		}

		if (actionType == 1) {
			news.setAgreeCount(Math.max(0, (news.getAgreeCount() == null ? 0 : news.getAgreeCount()) + delta));
		} else if (actionType == 3) {
			news.setShareCount(Math.max(0, (news.getShareCount() == null ? 0 : news.getShareCount()) + delta));
		} else {
			return;
		}
		newsService.updateById(news);
	}

	private Integer getActionCount(Long newsId, Byte actionType) {
		if (actionType == 2) {
			return Math.toIntExact(newsUserActionService.count(
				new QueryWrapper<NewsUserActionEntity>()
					.eq("news_id", newsId)
					.eq("action_type", actionType)
					.eq("is_deleted", 0)
			));
		}
		NewsEntity news = newsService.getById(newsId);
		if (news == null) {
			return 0;
		}
		if (actionType == 1) {
			return news.getAgreeCount() == null ? 0 : news.getAgreeCount();
		}
		return news.getShareCount() == null ? 0 : news.getShareCount();
	}
}
