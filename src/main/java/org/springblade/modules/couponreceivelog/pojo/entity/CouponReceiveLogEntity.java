package org.springblade.modules.couponreceivelog.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("coupon_receive_log")
@Schema(description = "领券日志/幂等表")
@EqualsAndHashCode(callSuper = true)
public class CouponReceiveLogEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "幂等请求ID")
	private String requestId;

	@Schema(description = "用户ID")
	private Long userId;

	@Schema(description = "券模板ID")
	private Long couponTemplateId;

	@Schema(description = "领取渠道 APP/WEB/H5/MINI_PROGRAM/ADMIN")
	private String receiveChannel;

	@Schema(description = "状态 1成功 2失败")
	private Integer status;

	@Schema(description = "失败原因")
	private String failReason;
}
