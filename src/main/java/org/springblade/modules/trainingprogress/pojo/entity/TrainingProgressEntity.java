package org.springblade.modules.trainingprogress.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.util.Date;

@Data
@TableName("ldqc_training_progress")
@EqualsAndHashCode(callSuper = true)
public class TrainingProgressEntity extends TenantEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;
	private Long trainingId;
	private Long lessonId;
	private Integer progressSeconds;
	private Integer durationSeconds;
	private Integer completed;
	private Date lastPlayAt;
}
