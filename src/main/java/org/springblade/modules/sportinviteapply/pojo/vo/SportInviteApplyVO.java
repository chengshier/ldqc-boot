package org.springblade.modules.sportinviteapply.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.sportinviteapply.pojo.entity.SportInviteApplyEntity;

import java.io.Serial;

/** 运动邀约申请运营视图。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SportInviteApplyVO extends SportInviteApplyEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "申请人昵称")
	private String applicantName;

	@Schema(description = "申请人头像")
	private String applicantAvatar;

	@Schema(description = "邀约标题")
	private String inviteTitle;
}
