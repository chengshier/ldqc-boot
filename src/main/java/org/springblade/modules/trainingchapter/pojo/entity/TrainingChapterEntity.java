package org.springblade.modules.trainingchapter.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("ldqc_training_chapter")
@EqualsAndHashCode(callSuper = true)
public class TrainingChapterEntity extends TenantEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long trainingId;
	private String title;
	private String description;
	private Integer sortOrder;
	private Integer status;
}
