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
package org.springblade.modules.userauthapply.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 用户认证申请表 实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@TableName("ldqc_user_auth_apply")
@Schema(description = "UserAuthApply对象")
@EqualsAndHashCode(callSuper = true)
public class UserAuthApplyEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	@Schema(description = "用户ID")
	private Long userId;
	/**
	 * 身份编码
	 */
	@Schema(description = "身份编码")
	private String authTypeCode;
	/**
	 * 身份名称
	 */
	@Schema(description = "身份名称")
	private String authTypeName;
	/**
	 * 表单数据JSON
	 */
	@Schema(description = "表单数据JSON")
	private String formData;
	/**
	 * 申请状态[1:审核中,2:已通过,3:已驳回,4:已撤回]
	 */
	@Schema(description = "申请状态[1:审核中,2:已通过,3:已驳回,4:已撤回]")
	private Integer applyStatus;
	/**
	 * 驳回原因
	 */
	@Schema(description = "驳回原因")
	private String auditReason;
	/**
	 * 最近审核人
	 */
	@Schema(description = "最近审核人")
	private Long lastAuditUser;
	/**
	 * 最近审核时间
	 */
	@Schema(description = "最近审核时间")
	private Date lastAuditTime;
	/**
	 * 通过时间
	 */
	@Schema(description = "通过时间")
	private Date approvedTime;

}
