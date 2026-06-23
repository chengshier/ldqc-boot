package org.springblade.modules.coupontemplate.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.util.Date;

@Data
@TableName("coupon_template")
@Schema(description = "优惠券模板")
@EqualsAndHashCode(callSuper = true)
public class CouponTemplateEntity extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "券编码")
    private String couponCode;

    @Schema(description = "券名称")
    private String couponName;

    @Schema(description = "券类型 CASH/DISCOUNT/FREE/DURATION/TIMES/SKU_DEDUCT")
    private String couponType;

    @Schema(description = "权益模式 AMOUNT/DURATION/TIMES/SKU_DEDUCT")
    private String benefitMode;

    @Schema(description = "满减门槛(分)")
    private Integer thresholdAmount;

    @Schema(description = "减免金额(分)或折扣值")
    private Integer discountAmount;

    @Schema(description = "最大减免金额(分)")
    private Integer maxDiscountAmount;

    @Schema(description = "时长券总分钟")
    private Integer durationMinutes;

    @Schema(description = "次数券总次数")
    private Integer totalTimes;

    @Schema(description = "抵扣目标类型 SPU/SKU/CATEGORY/SERVICE")
    private String deductTargetType;

    @Schema(description = "抵扣目标ID")
    private String deductTargetId;

    @Schema(description = "单位抵扣金额(分)")
    private Integer deductUnitAmount;

    @Schema(description = "适用范围 ALL/VENUE/CAMP/COURSE/GOODS")
    private String scopeType;

    @Schema(description = "范围引用ID")
    private String scopeRefId;

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "剩余库存")
    private Integer remainStock;

    @Schema(description = "每用户限领数")
    private Integer perUserLimit;

    @Schema(description = "最低成长等级")
    private Integer minGrowthLevel;

    @Schema(description = "有效期类型 FIXED/RELATIVE")
    private String validType;

    @Schema(description = "固定生效开始时间")
    private Date validStartAt;

    @Schema(description = "固定生效结束时间")
    private Date validEndAt;

    @Schema(description = "领取后有效天数")
    private Integer validDays;

    @Schema(description = "获取方式 FREE/POINTS_EXCHANGE")
    private String acquireType;

    @Schema(description = "兑换所需绿豆")
    private Integer costPoints;

    @Schema(description = "状态 1生效 0停用")
    private Integer status;

    @Schema(description = "扩展配置(JSON字符串)")
    private String extJson;
}
