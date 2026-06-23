package org.springblade.modules.pointstasklog.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("points_task_log")
@Schema(description = "PointsTaskLog对象")
@EqualsAndHashCode(callSuper = true)
public class PointsTaskLogEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String requestId;
	private Long userId;
	private String ruleCode;
	private String bizType;
	private String bizId;
	private Integer status;
	private String rejectReason;
}
