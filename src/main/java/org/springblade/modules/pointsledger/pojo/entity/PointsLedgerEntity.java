package org.springblade.modules.pointsledger.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("points_ledger")
@Schema(description = "PointsLedger对象")
@EqualsAndHashCode(callSuper = true)
public class PointsLedgerEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;
	private String changeType;
	private Integer changePoints;
	private Integer beforePoints;
	private Integer afterPoints;
	private String ruleCode;
	private String bizType;
	private String bizId;
	private String remark;
	private java.util.Date expiresAt;
	private String requestId;
}
