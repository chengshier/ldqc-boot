package org.springblade.modules.sportinvite.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.sportinvite.pojo.entity.SportInviteEntity;

import java.io.Serial;

/**
 * 运动邀约表 视图实体类
 *
 * @author BladeX
 * @since 2026-05-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SportInviteVO extends SportInviteEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "当前用户申请状态")
	private String myApplyStatus;

	@Schema(description = "联系方式是否可见")
	private Boolean contactVisible = false;

	@Schema(description = "当前用户是否可审核")
	private Boolean canAudit = false;

}
