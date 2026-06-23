/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 * <p>
 * Use of this software is governed by the Commercial License Agreement
 * obtained after purchasing a license from BladeX.
 * <p>
 * 1. This software is for development use only under a valid license
 * from BladeX.
 * <p>
 * 2. Redistribution of this software's source code to any third party
 * without a commercial license is strictly prohibited.
 * <p>
 * 3. Licensees may copyright their own code but cannot use segments
 * from this software for such purposes. Copyright of this software
 * remains with BladeX.
 * <p>
 * Using this software signifies agreement to this License, and the software
 * must not be used for illegal purposes.
 * <p>
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY. The author is
 * not liable for any claims arising from secondary or illegal development.
 * <p>
 * Author: Chill Zhuang (bladejava@qq.com)
 */
package org.springblade.modules.sportinviteauditlog.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 运动邀约审核日志表 实体类
 *
 * @author BladeX
 * @since 2026-05-21
 */
@Data
@TableName("ldqc_sport_invite_audit_log")
@Schema(description = "SportInviteAuditLog对象")
@EqualsAndHashCode(callSuper = true)
public class SportInviteAuditLogEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 邀约ID
	 */
	@Schema(description = "邀约ID")
	private Long inviteId;
	/**
	 * 申请ID
	 */
	@Schema(description = "申请ID")
	private Long applyId;
	/**
	 * 审核人ID
	 */
	@Schema(description = "审核人ID")
	private Long auditUserId;
	/**
	 * 审核动作：APPROVE通过 REJECT拒绝
	 */
	@Schema(description = "审核动作：APPROVE通过 REJECT拒绝")
	private String auditAction;
	/**
	 * 审核备注
	 */
	@Schema(description = "审核备注")
	private String auditRemark;

}
