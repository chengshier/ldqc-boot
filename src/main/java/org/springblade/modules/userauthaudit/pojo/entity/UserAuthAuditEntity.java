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
package org.springblade.modules.userauthaudit.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 用户认证审核日志表 实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@TableName("ldqc_user_auth_audit")
@Schema(description = "UserAuthAudit对象")
@EqualsAndHashCode(callSuper = true)
public class UserAuthAuditEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 申请ID
	 */
	@Schema(description = "申请ID")
	private Long applyId;
	/**
	 * 申请用户ID
	 */
	@Schema(description = "申请用户ID")
	private Long userId;
	/**
	 * 身份编码
	 */
	@Schema(description = "身份编码")
	private String authTypeCode;
	/**
	 * 审核状态[1:通过,2:驳回]
	 */
	@Schema(description = "审核状态[1:通过,2:驳回]")
	private Integer auditStatus;
	/**
	 * 审核意见
	 */
	@Schema(description = "审核意见")
	private String auditOpinion;
	/**
	 * 审核人
	 */
	@Schema(description = "审核人")
	private Long auditUser;
	/**
	 * 审核时间
	 */
	@Schema(description = "审核时间")
	private Date auditTime;
//	/**
//	 * 申请ID
//	 */
//	@Schema(description = "申请ID")
//	private Long applyId;
//	/**
//	 * 申请用户ID
//	 */
//	@Schema(description = "申请用户ID")
//	private Long userId;
//	/**
//	 * 身份编码
//	 */
//	@Schema(description = "身份编码")
//	private String authTypeCode;
//	/**
//	 * 审核状态[1:通过,2:驳回]
//	 */
//	@Schema(description = "审核状态[1:通过,2:驳回]")
//	private Integer auditStatus;
//	/**
//	 * 审核意见
//	 */
//	@Schema(description = "审核意见")
//	private String auditOpinion;
//	/**
//	 * 审核人
//	 */
//	@Schema(description = "审核人")
//	private Long auditUser;
//	/**
//	 * 审核时间
//	 */
//	@Schema(description = "审核时间")
//	private Date auditTime;

}
