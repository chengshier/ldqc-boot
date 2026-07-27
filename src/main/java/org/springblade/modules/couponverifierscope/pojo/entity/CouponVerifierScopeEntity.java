package org.springblade.modules.couponverifierscope.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("coupon_verifier_scope")
@EqualsAndHashCode(callSuper = true)
public class CouponVerifierScopeEntity extends TenantEntity {
	@Serial
	private static final long serialVersionUID = 1L;
	private Long verifierUserId;
	private String scopeType;
	private String scopeRefId;
	private String venueName;
}
