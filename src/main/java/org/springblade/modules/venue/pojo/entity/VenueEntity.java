package org.springblade.modules.venue.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 体育场馆。
 *
 * <p>ownerUserId 为审核通过后绑定的场馆运营账号。普通用户只能维护自己绑定的场馆，
 * 管理员可处理异常数据。status 使用基础实体字段：1公开、0停用。</p>
 */
@Data
@TableName("ldqc_venue")
@Schema(description = "体育场馆")
@EqualsAndHashCode(callSuper = true)
public class VenueEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String name;
	private String coverImage;
	/** 图集，逗号分隔或 JSON 数组。 */
	private String images;
	private String address;
	private BigDecimal longitude;
	private BigDecimal latitude;
	private BigDecimal rating;
	private String tags;
	private String businessHours;
	private String phone;
	private String description;
	private Long typeId;
	private String sortOrder;

	/** 场馆运营用户ID。 */
	private Long ownerUserId;
	/** 来源入驻申请ID；平台直接创建时为空。 */
	private Long sourceApplyId;
	/** 场馆主体或商户名称。 */
	private String merchantName;
	/** 对外服务说明，如预约方式、入场须知。 */
	private String serviceNotice;
}
