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
package org.springblade.modules.venuespace.excel;


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
 * 场馆场地表 Excel实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class VenueSpaceExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 场馆ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("场馆ID")
	private Long venueId;
	/**
	 * 场地名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("场地名称")
	private String spaceName;
	/**
	 * 场地类型
	 */
	@ColumnWidth(20)
	@ExcelProperty("场地类型")
	private String spaceType;
	/**
	 * 价格
	 */
	@ColumnWidth(20)
	@ExcelProperty("价格")
	private BigDecimal price;
	/**
	 * 容量
	 */
	@ColumnWidth(20)
	@ExcelProperty("容量")
	private Integer capacity;
	/**
	 * 图集,逗号分隔
	 */
	@ColumnWidth(20)
	@ExcelProperty("图集,逗号分隔")
	private String images;
	/**
	 * 场地介绍
	 */
	@ColumnWidth(20)
	@ExcelProperty("场地介绍")
	private String description;
	/**
	 * 排序
	 */
	@ColumnWidth(20)
	@ExcelProperty("排序")
	private Integer sortOrder;
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
