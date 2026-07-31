package org.springblade.modules.mallproduct.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.util.Date;

@Data
@TableName("mall_product")
@Schema(description = "积分商城商品")
@EqualsAndHashCode(callSuper = true)
public class MallProductEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String productCode;
	private String productName;
	private String productDesc;
	/** 商品业务类型 EQUIPMENT/DIGITAL/OTHER，优惠券保持独立业务。 */
	private String productType;
	private String coverUrl;
	private String galleryJson;
	private String categoryCode;
	private String categoryName;
	private String specJson;
	private String exchangeNotice;
	private Integer salePoints;
	/** 市场价，分 */
	private Integer marketAmount;
	private Integer stockTotal;
	private Integer stockAvailable;
	private Integer soldQty;
	/** 履约类型 SHIP/PICKUP/VIRTUAL */
	private String fulfillmentType;
	private Long merchantId;
	private String merchantName;
	private String pickupAddress;
	/** 每人累计限兑数量，0不限制 */
	private Integer perUserLimit;
	/** 单次最大兑换数量 */
	private Integer maxQtyPerOrder;
	private Integer requireAddress;
	private Date publishedAt;
	/** 1上架0下架 */
	private Integer status;
	private Integer sortNo;
	private String extJson;
}
