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
package org.springblade.modules.userauthapply.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 用户认证申请表 Excel实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class UserAuthApplyExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("用户ID")
	private Long userId;
	/**
	 * 身份编码
	 */
	@ColumnWidth(20)
	@ExcelProperty("身份编码")
	private String authTypeCode;
	/**
	 * 身份名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("身份名称")
	private String authTypeName;
	/**
	 * 表单数据JSON
	 */
	@ColumnWidth(20)
	@ExcelProperty("表单数据JSON")
	private String formData;
	/**
	 * 申请状态[1:审核中,2:已通过,3:已驳回,4:已撤回]
	 */
	@ColumnWidth(20)
	@ExcelProperty("申请状态[1:审核中,2:已通过,3:已驳回,4:已撤回]")
	private Integer applyStatus;
	/**
	 * 驳回原因
	 */
	@ColumnWidth(20)
	@ExcelProperty("驳回原因")
	private String auditReason;
	/**
	 * 最近审核人
	 */
	@ColumnWidth(20)
	@ExcelProperty("最近审核人")
	private Long lastAuditUser;
	/**
	 * 最近审核时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("最近审核时间")
	private Date lastAuditTime;
	/**
	 * 通过时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("通过时间")
	private Date approvedTime;
	/**
	 * 是否已删除
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否已删除")
	private Integer isDeleted;
	/**
	 * 租户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("租户ID")
	private String tenantId;

}
