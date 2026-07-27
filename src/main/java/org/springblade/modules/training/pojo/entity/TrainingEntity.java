package org.springblade.modules.training.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 培训课程主表。
 */
@Data
@TableName("ldqc_training")
@Schema(description = "培训课程")
@EqualsAndHashCode(callSuper = true)
public class TrainingEntity extends TenantEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	private String title;
	private String coverImage;
	private String instructorName;
	private BigDecimal price;
	/** 线下课程时长，分钟 */
	private Integer duration;
	private String location;
	private String address;
	private BigDecimal longitude;
	private BigDecimal latitude;
	private String category;
	private String description;
	private Integer sortOrder;
	/** 启用状态 */
	private Integer status;
	private Long orgId;
	private Long teacherId;
	private String courseType;

	/** 课程形态 OFFLINE/ONLINE/MIXED */
	private String contentMode;
	/** 发布状态 DRAFT/PENDING/PUBLISHED/REJECTED/OFFLINE */
	private String publishStatus;
	/** 达人课程所属用户；平台课程为空 */
	private Long talentUserId;
	/** 是否需要购买或授权 */
	private Integer purchaseRequired;
	private Integer totalLessons;
	/** 视频总时长，秒 */
	private Integer totalVideoDuration;
	private String auditReason;
}
