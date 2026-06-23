package org.springblade.modules.pointsaccount.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("points_account")
@Schema(description = "PointsAccount对象")
@EqualsAndHashCode(callSuper = true)
public class PointsAccountEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;
	private Integer availablePoints;
	private Integer frozenPoints;
	private Integer totalEarnedPoints;
	private Integer totalSpentPoints;
	private Integer growthLevel;
	private Integer version;
}
