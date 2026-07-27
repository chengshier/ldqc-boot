package org.springblade.modules.usercoupon.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "优惠券核销确认请求")
public class UserCouponVerifyConfirmRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "用户券ID")
	private Long userCouponId;

	@Schema(description = "核销模式 FULL/PARTIAL")
	private String verifyMode;

	@Schema(description = "本次核销分钟数")
	private Integer consumeDurationMinutes;

	@Schema(description = "本次核销次数")
	private Integer consumeTimes;
}
