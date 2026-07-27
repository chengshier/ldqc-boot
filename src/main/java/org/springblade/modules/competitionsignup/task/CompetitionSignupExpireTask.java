package org.springblade.modules.competitionsignup.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.competitionsignup.service.CompetitionSignupWorkflowService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时关闭超时未支付报名订单并释放赛事名额。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompetitionSignupExpireTask {

	private final CompetitionSignupWorkflowService workflowService;

	@Scheduled(cron = "0 */2 * * * ?")
	public void execute() {
		try {
			int count = workflowService.expireUnpaidOrders();
			if (count > 0) log.info("已关闭 {} 个超时赛事报名订单", count);
		} catch (Exception exception) {
			log.error("关闭超时赛事报名订单失败", exception);
		}
	}
}
