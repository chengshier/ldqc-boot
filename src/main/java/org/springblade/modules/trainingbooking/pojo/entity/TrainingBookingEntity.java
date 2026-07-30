package org.springblade.modules.trainingbooking.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("ldqc_training_booking")
@Schema(description = "体育课程线下预约")
@EqualsAndHashCode(callSuper = true)
public class TrainingBookingEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String bookingNo;
	private String requestId;
	private Long userId;
	private Long trainingId;
	private String trainingTitleSnapshot;
	private String coverImageSnapshot;
	private String contentModeSnapshot;
	private String courseTypeSnapshot;
	private BigDecimal priceSnapshot;
	private String locationSnapshot;
	private String addressSnapshot;
	private String contactName;
	private String contactPhone;
	private Integer participantCount;
	private String preferredTime;
	private String remark;
	private String bookingStatus;
	private String auditReason;
	private Date confirmedAt;
	private Date completedAt;
	private Date cancelledAt;
}
