package org.springblade.modules.pointssigninstat.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("points_signin_stat")
@Schema(description = "PointsSigninStat对象")
@EqualsAndHashCode(callSuper = true)
public class PointsSigninStatEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;
	private java.util.Date lastSigninDate;
	private Integer continueDays;
	private Integer monthSigninDays;
}
