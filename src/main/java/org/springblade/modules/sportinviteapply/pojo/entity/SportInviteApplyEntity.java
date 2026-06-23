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
package org.springblade.modules.sportinviteapply.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 运动邀约申请表 实体类
 *
 * @author BladeX
 * @since 2026-05-21
 */
@Data
@TableName("ldqc_sport_invite_apply")
@Schema(description = "SportInviteApply对象")
@EqualsAndHashCode(callSuper = true)
public class SportInviteApplyEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 申请状态：PENDING待审核 APPROVED已通过 REJECTED已拒绝 CANCELED已取消
	 */
	@Schema(description = "申请状态：PENDING待审核 APPROVED已通过 REJECTED已拒绝 CANCELED已取消")
	private String applyStatus;
	/**
	 * 邀约ID
	 */
	@Schema(description = "邀约ID")
	private Long inviteId;
	/**
	 * 申请人用户ID
	 */
	@Schema(description = "申请人用户ID")
	private Long applicantUserId;
	/**
	 * 申请人运动水平
	 */
	@Schema(description = "申请人运动水平")
	private String applicantLevel;
	/**
	 * 运动经验/频率
	 */
	@Schema(description = "运动经验/频率")
	private String sportExperience;
	/**
	 * 是否确认准时到场：1是 0否
	 */
	@Schema(description = "是否确认准时到场：1是 0否")
	private Integer punctualConfirm;
	/**
	 * 申请人手机号
	 */
	@Schema(description = "申请人手机号")
	private String contactPhone;
	/**
	 * 申请人微信号
	 */
	@Schema(description = "申请人微信号")
	private String contactWechat;
	/**
	 * 申请留言
	 */
	@Schema(description = "申请留言")
	private String applyMessage;
	/**
	 * 个人运动照片或历史记录，JSON数组
	 */
	@Schema(description = "个人运动照片或历史记录，JSON数组")
	private String applyImages;
	/**
	 * 审核人ID
	 */
	@Schema(description = "审核人ID")
	private Long auditUserId;
	/**
	 * 审核时间
	 */
	@Schema(description = "审核时间")
	private Date auditTime;
	/**
	 * 拒绝原因
	 */
	@Schema(description = "拒绝原因")
	private String rejectReason;

}
