package org.springblade.modules.contentaudit.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_audit_task")
public class ContentAuditTask extends TenantEntity {
	private String bizType;
	private Long bizId;
	private Long userId;
	private String openId;
	private String contentSnapshot;
	private Byte auditStatus;
	private String providerTraceId;
	private String resultCode;
	private String resultMessage;
	private Integer attemptCount;
	private Date nextRetryTime;
	private Date auditTime;
}
