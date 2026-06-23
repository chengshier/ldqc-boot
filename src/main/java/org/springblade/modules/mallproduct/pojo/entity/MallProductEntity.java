package org.springblade.modules.mallproduct.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("mall_product")
@Schema(description = "积分商城商品")
@EqualsAndHashCode(callSuper = true)
public class MallProductEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "商品编码")
	private String productCode;

	@Schema(description = "商品名称")
	private String productName;

	@Schema(description = "商品描述")
	private String productDesc;

	@Schema(description = "商品类型 PHYSICAL/VIRTUAL/COUPON")
	private String productType;

	@Schema(description = "商品封面图")
	private String coverUrl;

	@Schema(description = "兑换所需绿豆")
	private Integer salePoints;

	@Schema(description = "市场价(分)")
	private Integer marketAmount;

	@Schema(description = "库存总量")
	private Integer stockTotal;

	@Schema(description = "可用库存")
	private Integer stockAvailable;

	@Schema(description = "状态 1上架 0下架")
	private Integer status;

	@Schema(description = "排序号 越小越靠前")
	private Integer sortNo;

	@Schema(description = "扩展配置(JSON字符串)")
	private String extJson;
}
