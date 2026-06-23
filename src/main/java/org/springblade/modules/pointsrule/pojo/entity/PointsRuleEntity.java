package org.springblade.modules.pointsrule.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("points_rule")
@Schema(description = "PointsRule对象")
@EqualsAndHashCode(callSuper = true)
public class PointsRuleEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String ruleCode;
	private String ruleName;
	private String sceneType;
	private Integer grantPoints;
	private Integer dailyLimitCount;
	private Integer dailyLimitPoints;
	private Integer lifecycleLimitCount;
	private Integer requireFirstFlag;
	private Integer status;
	private String extJson;
}
