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
package org.springblade.modules.outdoor.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 户外活动表 Excel实体类
 *
 * @author BladeX
 * @since 2026-03-10
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class OutdoorExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 活动标题
	 */
	@ColumnWidth(20)
	@ExcelProperty("活动标题")
	private String title;
	/**
	 * 封面图URL
	 */
	@ColumnWidth(20)
	@ExcelProperty("封面图URL")
	private String coverImage;
	/**
	 * 活动截止日期
	 */
	@ColumnWidth(20)
	@ExcelProperty("活动截止日期")
	private Date endTime;
	/**
	 * 活动地点
	 */
	@ColumnWidth(20)
	@ExcelProperty("活动地点")
	private String location;
	/**
	 * 活动难度
	 */
	@ColumnWidth(20)
	@ExcelProperty("活动难度")
	private String difficulty;
	/**
	 * 组织者用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("组织者用户ID")
	private Long organizerId;
	/**
	 * 点赞/喜欢数
	 */
	@ColumnWidth(20)
	@ExcelProperty("点赞/喜欢数")
	private Integer likesCount;
	/**
	 * 活动描述
	 */
	@ColumnWidth(20)
	@ExcelProperty("活动描述")
	private String description;
	/**
	 * 活动状态
	 */
	@ColumnWidth(20)
	@ExcelProperty("活动状态")
	private Integer status;
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
