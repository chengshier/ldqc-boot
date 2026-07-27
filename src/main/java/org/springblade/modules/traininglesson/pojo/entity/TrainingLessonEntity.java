package org.springblade.modules.traininglesson.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("ldqc_training_lesson")
@EqualsAndHashCode(callSuper = true)
public class TrainingLessonEntity extends TenantEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long trainingId;
	private Long chapterId;
	private String title;
	private String lessonType;
	private String videoUrl;
	private String posterUrl;
	private Integer durationSeconds;
	private Integer isTrial;
	private String mediaProcessStatus;
	private Integer sortOrder;
	private Integer status;
}
