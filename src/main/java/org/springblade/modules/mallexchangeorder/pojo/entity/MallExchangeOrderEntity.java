package org.springblade.modules.mallexchangeorder.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.util.Date;

@Data
@TableName("mall_exchange_order")
@Schema(description = "积分商城兑换订单")
@EqualsAndHashCode(callSuper = true)
public class MallExchangeOrderEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String orderNo;
	private String requestId;
	private Long userId;
	private Long productId;
	private Integer qty;
	private Integer spendPoints;

	/** 商品快照 */
	private String productCodeSnapshot;
	private String productNameSnapshot;
	private String coverUrlSnapshot;
	private String specSnapshot;
	private Integer unitPoints;
	private String fulfillmentType;
	private String merchantNameSnapshot;

	/** 收货、领取和虚拟权益信息 */
	private String receiverName;
	private String receiverPhone;
	private String receiverAddress;
	private String pickupAddressSnapshot;
	private String pickupCode;
	private String virtualContent;
	private String logisticsCompany;
	private String logisticsNo;
	private String fulfillmentStatus;
	private String fulfillmentRemark;
	private Date completedAt;
	private Date cancelledAt;
	private String cancelReason;

	@Schema(description = "订单状态 CREATED/SUCCESS/FAILED/CANCELLED/COMPLETED")
	private String orderStatus;
	private String failReason;
	/** 历史兼容字段 NONE/PENDING/SENT/FINISHED */
	private String deliveryStatus;
}
