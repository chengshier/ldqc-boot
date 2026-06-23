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
package org.springblade.modules.sportinviteapply.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 运动邀约申请表 Excel实体类
 *
 * @author BladeX
 * @since 2026-05-21
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class SportInviteApplyExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键ID")
	private Long id;
	/**
	 * 租户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("租户ID")
	private String tenantId;
	/**
	 * 是否删除
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否删除")
	private Integer isDeleted;
	/**
	 * 申请状态：PENDING待审核 APPROVED已通过 REJECTED已拒绝 CANCELED已取消
	 */
	@ColumnWidth(20)
	@ExcelProperty("申请状态：PENDING待审核 APPROVED已通过 REJECTED已拒绝 CANCELED已取消")
	private String applyStatus;
	/**
	 * 邀约ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("邀约ID")
	private Long inviteId;
	/**
	 * 申请人用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("申请人用户ID")
	private Long applicantUserId;
	/**
	 * 申请人运动水平
	 */
	@ColumnWidth(20)
	@ExcelProperty("申请人运动水平")
	private String applicantLevel;
	/**
	 * 运动经验/频率
	 */
	@ColumnWidth(20)
	@ExcelProperty("运动经验/频率")
	private String sportExperience;
	/**
	 * 是否确认准时到场：1是 0否
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否确认准时到场：1是 0否")
	private Integer punctualConfirm;
	/**
	 * 申请人手机号
	 */
	@ColumnWidth(20)
	@ExcelProperty("申请人手机号")
	private String contactPhone;
	/**
	 * 申请人微信号
	 */
	@ColumnWidth(20)
	@ExcelProperty("申请人微信号")
	private String contactWechat;
	/**
	 * 申请留言
	 */
	@ColumnWidth(20)
	@ExcelProperty("申请留言")
	private String applyMessage;
	/**
	 * 个人运动照片或历史记录，JSON数组
	 */
	@ColumnWidth(20)
	@ExcelProperty("个人运动照片或历史记录，JSON数组")
	private String applyImages;
	/**
	 * 审核人ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("审核人ID")
	private Long auditUserId;
	/**
	 * 审核时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("审核时间")
	private Date auditTime;
	/**
	 * 拒绝原因
	 */
	@ColumnWidth(20)
	@ExcelProperty("拒绝原因")
	private String rejectReason;

}
