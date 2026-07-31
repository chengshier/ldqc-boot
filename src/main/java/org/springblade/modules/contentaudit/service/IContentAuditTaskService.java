package org.springblade.modules.contentaudit.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.contentaudit.pojo.entity.ContentAuditTask;

public interface IContentAuditTaskService extends BaseService<ContentAuditTask> {

	/** 重试当前已到期的审核任务，返回实际处理数量。 */
	int retryDueTasks(int batchSize);

	/** 立即重试指定任务。 */
	boolean retryNow(Long taskId);

	/** 运营人员人工判定，action 仅支持 PASS 或 REJECT。 */
	boolean manualResolve(Long taskId, String action, String reason);
}
