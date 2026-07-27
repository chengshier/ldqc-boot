package org.springblade.modules.trainingaccess.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.util.Date;

@Data
@TableName("ldqc_training_access")
@EqualsAndHashCode(callSuper = true)
public class TrainingAccessEntity extends TenantEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;
	private Long trainingId;
	private String sourceType;
	private String sourceId;
	private String accessStatus;
	private Date validStartAt;
	private Date validEndAt;
}
