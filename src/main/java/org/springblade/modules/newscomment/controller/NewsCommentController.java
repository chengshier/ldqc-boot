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
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.contentaudit.pojo.entity.ContentAuditTask;
import org.springblade.modules.contentaudit.service.IContentAuditTaskService;
import org.springblade.modules.contentaudit.service.WechatContentAuditService;
import org.springblade.modules.news.pojo.entity.NewsEntity;
import org.springblade.modules.news.service.INewsService;
import org.springblade.modules.newscomment.excel.NewsCommentExcel;
import org.springblade.modules.newscomment.pojo.entity.NewsCommentEntity;
import org.springblade.modules.newscomment.pojo.vo.NewsCommentVO;
import org.springblade.modules.newscomment.service.INewsCommentService;
import org.springblade.modules.newscomment.wrapper.NewsCommentWrapper;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.usermessage.pojo.entity.UserMessage;
import org.springblade.modules.usermessage.service.IUserMessageService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("blade-newscomment/newsComment")
@Tag(name = "新闻评论", description = "新闻评论公开查询、用户提交与运营管理接口")
public class NewsCommentController extends BladeController {

	private final INewsCommentService newsCommentService;
	private final INewsService newsService;
	private final WechatContentAuditService wechatContentAuditService;
	private final IContentAuditTaskService auditTaskService;
	private final IUserMessageService userMessageService;
	private final IUserService userService;

	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "管理端详情")
	public R<NewsCommentVO> detail(NewsCommentEntity newsComment) {
		NewsCommentEntity detail = newsCommentService.getOne(Condition.getQueryWrapper(newsComment));
		return R.data(NewsCommentWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "管理端列表")
	public R<IPage<NewsCommentVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> newsComment, Query query) {
		IPage<NewsCommentEntity> pages = newsCommentService.page(Condition.getPage(query), Condition.getQueryWrapper(newsComment, NewsCommentEntity.class));
		return R.data(NewsCommentWrapper.build().pageVO(pages));
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "管理端分页")
	public R<IPage<NewsCommentVO>> page(NewsCommentVO newsComment, Query query) {
		return R.data(newsCommentService.selectNewsCommentPage(Condition.getPage(query), newsComment));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "管理端新增")
	public R save(@Valid @RequestBody NewsCommentEntity newsComment) {
		return R.status(newsCommentService.save(newsComment));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "管理端修改")
	public R update(@Valid @RequestBody NewsCommentEntity newsComment) {
		return R.status(newsCommentService.updateById(newsComment));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "管理端新增或修改")
	public R submit(@Valid @RequestBody NewsCommentEntity newsComment) {
		return R.status(newsCommentService.saveOrUpdate(newsComment));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "管理端删除")
	public R remove(@Parameter(description = "ids", required = true) @RequestParam String ids) {
		return R.status(newsCommentService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-newsComment")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出新闻评论")
	public void exportNewsComment(@Parameter(hidden = true) @RequestParam Map<String, Object> newsComment, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<NewsCommentEntity> queryWrapper = Condition.getQueryWrapper(newsComment, NewsCommentEntity.class);
		List<NewsCommentExcel> list = newsCommentService.exportNewsComment(queryWrapper);
		ExcelUtil.export(response, "news_comment_" + DateUtil.time(), "news_comment", list, NewsCommentExcel.class);
	}

	@GetMapping("/mobile/page")
	@ApiOperationSupport(order = 20)
	@Operation(summary = "小程序评论分页", description = "只返回已通过审核的评论")
	public R<IPage<NewsCommentEntity>> mobilePage(@RequestParam Long newsId,
		@RequestParam(defaultValue = "1") Integer current,
		@RequestParam(defaultValue = "20") Integer size) {
		IPage<NewsCommentEntity> pages = newsCommentService.page(new Page<>(current, size),
			new QueryWrapper<NewsCommentEntity>()
				.eq("news_id", newsId)
				.eq("comment_status", WechatContentAuditService.PASSED)
				.eq("is_deleted", 0)
				.orderByDesc("create_time"));
		return R.data(pages);
	}

	@PostMapping("/mobile/save")
	@ApiOperationSupport(order = 21)
	@Operation(summary = "提交新闻评论", description = "用户身份、昵称和头像以后端登录态为准")
	public R<Map<String, Object>> mobileSave(@RequestBody NewsCommentEntity newsComment) {
		Long currentUserId = AuthUtil.getUserId();
		if (Func.isEmpty(currentUserId) || currentUserId <= 0) throw new ServiceException("请先登录后再发表评论");
		User currentUser = userService.getById(currentUserId);
		if (currentUser == null) throw new ServiceException("用户不存在");
		if (newsComment.getNewsId() == null) return R.fail("缺少新闻ID");
		if (Func.isBlank(newsComment.getContent())) return R.fail("评论内容不能为空");

		newsComment.setId(null);
		newsComment.setUserId(currentUserId);
		newsComment.setUsername(Func.isNotBlank(currentUser.getName()) ? currentUser.getName() : currentUser.getRealName());
		newsComment.setAvatar(currentUser.getAvatar());
		newsComment.setContent(newsComment.getContent().trim());
		newsComment.setParentId(newsComment.getParentId() == null ? 0L : newsComment.getParentId());
		newsComment.setLikeCount(0);

		WechatContentAuditService.AuditResult auditResult = wechatContentAuditService.audit(currentUserId, newsComment.getContent());
		newsComment.setCommentStatus(auditResult.status());
		newsComment.setAuditReason(auditResult.reason());
		if (auditResult.status() == WechatContentAuditService.PASSED || auditResult.status() == WechatContentAuditService.REJECTED) {
			newsComment.setAuditTime(new java.util.Date());
		}
		if (!newsCommentService.save(newsComment)) return R.fail("评论保存失败");

		ContentAuditTask task = new ContentAuditTask();
		task.setTenantId(newsComment.getTenantId());
		task.setBizType("NEWS_COMMENT");
		task.setBizId(newsComment.getId());
		task.setUserId(currentUserId);
		task.setContentSnapshot(newsComment.getContent());
		task.setAuditStatus(auditResult.status());
		task.setResultMessage(auditResult.reason());
		task.setAttemptCount(1);
		task.setAuditTime(newsComment.getAuditTime());
		if (auditResult.status() == WechatContentAuditService.RETRY) {
			task.setNextRetryTime(new java.util.Date(System.currentTimeMillis() + 60_000L));
		}
		auditTaskService.save(task);
		newsComment.setAuditTaskId(task.getId());
		newsCommentService.updateById(newsComment);

		if (auditResult.status() == WechatContentAuditService.REJECTED) {
			UserMessage message = new UserMessage();
			message.setTenantId(newsComment.getTenantId());
			message.setUserId(currentUserId);
			message.setMessageType("COMMENT_AUDIT_REJECT");
			message.setTitle("评论未通过审核");
			message.setContent(auditResult.reason());
			message.setBizType("NEWS_COMMENT");
			message.setBizId(newsComment.getId());
			message.setReadStatus((byte) 0);
			userMessageService.save(message);
		}

		NewsEntity news = newsService.getById(newsComment.getNewsId());
		if (news != null && auditResult.status() == WechatContentAuditService.PASSED) {
			news.setCommentCount((news.getCommentCount() == null ? 0 : news.getCommentCount()) + 1);
			newsService.updateById(news);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("comment", newsComment);
		result.put("commentCount", news != null ? news.getCommentCount() : null);
		return R.data(result);
	}
}
