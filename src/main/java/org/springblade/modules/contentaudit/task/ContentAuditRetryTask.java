package org.springblade.modules.contentaudit.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.contentaudit.service.IContentAuditTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 定时重试因微信审核服务异常而暂未完成的评论审核任务。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentAuditRetryTask {

	private final IContentAuditTaskService auditTaskService;

	@Scheduled(
		initialDelayString = "${content-audit.retry.initial-delay-ms:30000}",
		fixedDelayString = "${content-audit.retry.fixed-delay-ms:60000}"
	)
	public void execute() {
		try {
			int processed = auditTaskService.retryDueTasks(50);
			if (processed > 0) {
				log.info("content audit retry completed, processed={}", processed);
			}
		} catch (Exception exception) {
			log.error("content audit retry task failed", exception);
		}
	}
}
