package org.springblade.modules.usercoupon.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.util.Date;

@Data
@TableName("user_coupon")
@Schema(description = "用户优惠券")
@EqualsAndHashCode(callSuper = true)
public class UserCouponEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "用户ID")
	private Long userId;

	@Schema(description = "券模板ID")
	private Long couponTemplateId;

	@Schema(description = "券号")
	private String couponNo;

	@TableField("status")
	@Schema(description = "券状态 UNUSED/LOCKED/USED/EXPIRED/INVALID")
	private String couponStatus;

	@Schema(description = "剩余时长(分钟)")
	private Integer remainDurationMinutes;

	@Schema(description = "剩余次数")
	private Integer remainTimes;

	@Schema(description = "有效期开始时间")
	private Date validStartAt;

	@Schema(description = "有效期结束时间")
	private Date validEndAt;

	@Schema(description = "锁定订单号")
	private String lockedOrderNo;

	@Schema(description = "使用订单号")
	private String usedOrderNo;

	@Schema(description = "使用时间")
	private Date usedAt;

	@Schema(description = "核销商家用户ID")
	private Long verifyMerchantUserId;

	@Schema(description = "核销时间")
	private Date verifyAt;
}
