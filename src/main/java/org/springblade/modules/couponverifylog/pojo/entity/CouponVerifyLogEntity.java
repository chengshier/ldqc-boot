package org.springblade.modules.couponverifylog.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("coupon_verify_log")
@Schema(description = "优惠券核销日志")
@EqualsAndHashCode(callSuper = true)
public class CouponVerifyLogEntity extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户券ID")
    private Long userCouponId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商家用户ID")
    private Long merchantUserId;

    @Schema(description = "券模板ID")
    private Long templateId;

    @Schema(description = "券号")
    private String couponNo;

    @Schema(description = "核销渠道 APP/WEB/H5/MINI_PROGRAM/ADMIN")
    private String verifyChannel;

    @Schema(description = "核销结果 1成功 0失败")
    private Integer verifyResult;

    @Schema(description = "失败原因")
    private String failReason;

    @TableField("verify_status")
    @Schema(description = "状态 PROCESSING/FINISHED")
    private String verifyStatus;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "扩展JSON")
    private String extJson;
}
