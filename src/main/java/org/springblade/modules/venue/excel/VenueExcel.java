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
package org.springblade.modules.venue.excel;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 体育场馆表 Excel实体类
 *
 * @author BladeX
 * @since 2026-03-10
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class VenueExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 场馆名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("场馆名称")
	private String name;
	/**
	 * 场馆主图URL
	 */
	@ColumnWidth(20)
	@ExcelProperty("场馆主图URL")
	private String coverImage;
	/**
	 * 图集,逗号分隔
	 */
	@ColumnWidth(20)
	@ExcelProperty("图集,逗号分隔")
	private String images;
	/**
	 * 详细地址
	 */
	@ColumnWidth(20)
	@ExcelProperty("详细地址")
	private String address;
	/**
	 * 经度
	 */
	@ColumnWidth(20)
	@ExcelProperty("经度")
	private BigDecimal longitude;
	/**
	 * 纬度
	 */
	@ColumnWidth(20)
	@ExcelProperty("纬度")
	private BigDecimal latitude;
	/**
	 * 评分
	 */
	@ColumnWidth(20)
	@ExcelProperty("评分")
	private BigDecimal rating;
	/**
	 * 标签,逗号分隔
	 */
	@ColumnWidth(20)
	@ExcelProperty("标签,逗号分隔")
	private String tags;
	/**
	 * 营业时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("营业时间")
	private String businessHours;
	/**
	 * 联系电话
	 */
	@ColumnWidth(20)
	@ExcelProperty("联系电话")
	private String phone;
	/**
	 * 场馆介绍
	 */
	@ColumnWidth(20)
	@ExcelProperty("场馆介绍")
	private String description;
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
