package org.springblade.modules.coupontemplate.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.util.Date;

/** 优惠券模板。领取资格使用显式字段，不再依赖客户端参数或模糊 JSON 判断。 */
@Data
@TableName("coupon_template")
@Schema(description = "优惠券模板")
@EqualsAndHashCode(callSuper = true)
public class CouponTemplateEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String couponCode;
	private String couponName;
	/** CASH/DISCOUNT/FREE/DURATION/TIMES/SKU_DEDUCT */
	private String couponType;
	/** AMOUNT/DURATION/TIMES/SKU_DEDUCT */
	private String benefitMode;
	/** 金额字段单位为分。 */
	private Integer thresholdAmount;
	private Integer discountAmount;
	private Integer maxDiscountAmount;
	private Integer durationMinutes;
	private Integer totalTimes;
	private String deductTargetType;
	private String deductTargetId;
	private Integer deductUnitAmount;
	/** ALL/VENUE/CAMP/COURSE/GOODS */
	private String scopeType;
	private String scopeRefId;
	private Integer totalStock;
	private Integer remainStock;
	private Integer perUserLimit;
	private Integer minGrowthLevel;
	/** 1要求完成认证，0不要求。 */
	private Integer authRequired;
	/** 可领取开始时间。 */
	private Date receiveStartAt;
	/** 可领取结束时间。 */
	private Date receiveEndAt;
	/** FIXED/RELATIVE */
	private String validType;
	private Date validStartAt;
	private Date validEndAt;
	private Integer validDays;
	/** FREE/POINTS_EXCHANGE */
	private String acquireType;
	private Integer costPoints;
	/** 1生效0停用 */
	private Integer status;
	private String extJson;
}
