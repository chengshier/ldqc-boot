package org.springblade.modules.mallexchangeorder.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("mall_exchange_order")
@Schema(description = "积分商城兑换订单")
@EqualsAndHashCode(callSuper = true)
public class MallExchangeOrderEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "订单号")
	private String orderNo;

	@Schema(description = "幂等请求ID")
	private String requestId;

	@Schema(description = "用户ID")
	private Long userId;

	@Schema(description = "商品ID")
	private Long productId;

	@Schema(description = "兑换数量")
	private Integer qty;

	@Schema(description = "消耗绿豆")
	private Integer spendPoints;

	@TableField("status")
	@Schema(description = "订单状态 INIT/SUCCESS/FAILED/CANCELLED")
	private String orderStatus;

	@Schema(description = "失败原因")
	private String failReason;

	@Schema(description = "发货状态 NONE/PENDING/SENT/FINISHED")
	private String deliveryStatus;
}
