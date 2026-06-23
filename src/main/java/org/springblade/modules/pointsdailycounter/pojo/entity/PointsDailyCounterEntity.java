package org.springblade.modules.pointsdailycounter.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("points_daily_counter")
@Schema(description = "PointsDailyCounter对象")
@EqualsAndHashCode(callSuper = true)
public class PointsDailyCounterEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;
	private java.util.Date statDate;
	private String sceneType;
	private Integer grantCount;
	private Integer grantPoints;
}
