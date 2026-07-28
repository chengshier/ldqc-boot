package org.springblade.modules.contentaudit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.contentaudit.pojo.entity.ContentAuditTask;
import org.springblade.modules.contentaudit.service.ContentAuditTaskServiceImpl;
import org.springblade.modules.contentaudit.service.IContentAuditTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** 动态文案、媒体与评论的自动审核异常运营接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("blade-contentaudit/task")
@Tag(name = "内容自动审核异常", description = "微信自动审核重试、复核与人工判定")
public class ContentAuditTaskController {

	private final IContentAuditTaskService auditTaskService;

	@IsAdmin
	@GetMapping("/page")
	@Operation(summary = "异常任务分页", description = "默认显示自动重试中和待人工处理的动态文案、媒体及评论任务")
	public R<IPage<ContentAuditTask>> page(Query query,
		@RequestParam(required = false) String bizType,
		@RequestParam(required = false) Byte auditStatus) {
		IPage<ContentAuditTask> page = auditTaskService.page(Condition.getPage(query),
			Wrappers.<ContentAuditTask>lambdaQuery()
				.eq(Func.isNotBlank(bizType), ContentAuditTask::getBizType, bizType)
				.eq(auditStatus != null, ContentAuditTask::getAuditStatus, auditStatus)
				.in(auditStatus == null, ContentAuditTask::getAuditStatus,
					ContentAuditTaskServiceImpl.RETRY, ContentAuditTaskServiceImpl.MANUAL_REQUIRED)
				.eq(ContentAuditTask::getIsDeleted, 0)
				.orderByDesc(ContentAuditTask::getAuditStatus)
				.orderByAsc(ContentAuditTask::getNextRetryTime)
				.orderByDesc(ContentAuditTask::getCreateTime));
		return R.data(page);
	}

	@IsAdmin
	@GetMapping("/summary")
	@Operation(summary = "异常任务汇总")
	public R<Map<String, Long>> summary() {
		Map<String, Long> result = new LinkedHashMap<>();
		result.put("retrying", auditTaskService.count(Wrappers.<ContentAuditTask>lambdaQuery()
			.eq(ContentAuditTask::getAuditStatus, ContentAuditTaskServiceImpl.RETRY)
			.eq(ContentAuditTask::getIsDeleted, 0)));
		result.put("manualRequired", auditTaskService.count(Wrappers.<ContentAuditTask>lambdaQuery()
			.eq(ContentAuditTask::getAuditStatus, ContentAuditTaskServiceImpl.MANUAL_REQUIRED)
			.eq(ContentAuditTask::getIsDeleted, 0)));
		return R.data(result);
	}

	@IsAdmin
	@PostMapping("/retry-now")
	@Operation(summary = "立即重新审核", description = "文本重新同步检测，媒体重新提交异步检测")
	public R retryNow(@RequestBody Map<String, Object> body) {
		Long taskId = Func.toLong(body.get("taskId"));
		return R.status(auditTaskService.retryNow(taskId));
	}

	@IsAdmin
	@PostMapping("/resolve")
	@Operation(summary = "人工通过或拒绝", description = "动态任务处理后会重新汇总整条动态，全部通过才公开")
	public R resolve(@RequestBody Map<String, Object> body) {
		Long taskId = Func.toLong(body.get("taskId"));
		String action = Func.toStr(body.get("action"));
		String reason = Func.toStr(body.get("reason"));
		return R.status(auditTaskService.manualResolve(taskId, action, reason));
	}
}
