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
package org.springblade.modules.sportinviteauditlog.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 运动邀约审核日志表 Excel实体类
 *
 * @author BladeX
 * @since 2026-05-21
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class SportInviteAuditLogExcel implements Serializable {

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
	 * 邀约ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("邀约ID")
	private Long inviteId;
	/**
	 * 申请ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("申请ID")
	private Long applyId;
	/**
	 * 审核人ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("审核人ID")
	private Long auditUserId;
	/**
	 * 审核动作：APPROVE通过 REJECT拒绝
	 */
	@ColumnWidth(20)
	@ExcelProperty("审核动作：APPROVE通过 REJECT拒绝")
	private String auditAction;
	/**
	 * 审核备注
	 */
	@ColumnWidth(20)
	@ExcelProperty("审核备注")
	private String auditRemark;

}
