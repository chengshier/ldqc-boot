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
package org.springblade.modules.sportinvitemedia.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 运动邀约媒体表 Excel实体类
 *
 * @author BladeX
 * @since 2026-05-21
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class SportInviteMediaExcel implements Serializable {

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
	 * 媒体状态：NORMAL正常 DISABLED禁用
	 */
	@ColumnWidth(20)
	@ExcelProperty("媒体状态：NORMAL正常 DISABLED禁用")
	private String mediaStatus;
	/**
	 * 邀约ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("邀约ID")
	private Long inviteId;
	/**
	 * 媒体地址
	 */
	@ColumnWidth(20)
	@ExcelProperty("媒体地址")
	private String mediaUrl;
	/**
	 * 媒体类型：IMAGE图片
	 */
	@ColumnWidth(20)
	@ExcelProperty("媒体类型：IMAGE图片")
	private String mediaType;
	/**
	 * 排序
	 */
	@ColumnWidth(20)
	@ExcelProperty("排序")
	private Integer sortNo;

}
