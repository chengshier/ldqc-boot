package org.springblade.modules.userinterest.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

/** 用户选择的运动分类兴趣。 */
@Data
@TableName("ldqc_user_interest")
@EqualsAndHashCode(callSuper = true)
public class UserInterestEntity extends TenantEntity {
	@Serial
	private static final long serialVersionUID = 1L;
	private Long userId;
	private Long categoryId;
	private Integer sort;
}
