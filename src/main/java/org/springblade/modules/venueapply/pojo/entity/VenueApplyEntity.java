package org.springblade.modules.venueapply.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/** 场馆入驻申请。 */
@Data
@TableName("ldqc_venue_apply")
@Schema(description = "场馆入驻申请")
@EqualsAndHashCode(callSuper = true)
public class VenueApplyEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String requestNo;
	private String activeUniqueKey;
	private Long applicantUserId;
	private String applicantName;
	private String applicantPhone;
	private String merchantName;
	private String licenseNo;
	private String licenseImage;
	private String venueName;
	private Long venueTypeId;
	private String coverImage;
	private String images;
	private String address;
	private BigDecimal longitude;
	private BigDecimal latitude;
	private String businessHours;
	private String venuePhone;
	private String tags;
	private String description;
	private String serviceNotice;
	/** PENDING/APPROVED/REJECTED/CANCELLED。 */
	private String applyStatus;
	private Long auditUserId;
	private Date auditTime;
	private String auditReason;
	private Long venueId;
	private Date submittedAt;
}
