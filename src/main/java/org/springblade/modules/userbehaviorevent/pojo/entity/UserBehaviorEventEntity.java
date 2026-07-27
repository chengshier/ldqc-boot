package org.springblade.modules.userbehaviorevent.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.util.Date;

@Data
@TableName("user_behavior_event")
@Schema(description = "UserBehaviorEvent对象")
@EqualsAndHashCode(callSuper = true)
public class UserBehaviorEventEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String eventCode;
	private Long userId;
	private String bizType;
	private String bizId;
	private Integer eventStatus;
	private String requestId;
	private String source;
	private Date eventTime;
	private String extJson;
}
