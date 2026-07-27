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
package org.springblade.modules.bannerposition.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 宣传Banner位置表 Excel实体类
 *
 * @author BladeX
 * @since 2026-07-06
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class BannerPositionExcel implements Serializable {

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
	 * 位置编码，如 home_training
	 */
	@ColumnWidth(20)
	@ExcelProperty("位置编码，如 home_training")
	private String code;
	/**
	 * 位置名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("位置名称")
	private String name;
	/**
	 * 终端类型，如 miniapp/h5/all
	 */
	@ColumnWidth(20)
	@ExcelProperty("终端类型，如 miniapp/h5/all")
	private String terminal;
	/**
	 * 页面编码，如 home_index
	 */
	@ColumnWidth(20)
	@ExcelProperty("页面编码，如 home_index")
	private String pageCode;
	/**
	 * 频道编码，如 training/venue/outdoor
	 */
	@ColumnWidth(20)
	@ExcelProperty("频道编码，如 training/venue/outdoor")
	private String channelCode;
	/**
	 * 展示样式，如 image_only/image_text_bottom_left/image_text_center_badge
	 */
	@ColumnWidth(20)
	@ExcelProperty("展示样式，如 image_only/image_text_bottom_left/image_text_center_badge")
	private String displayStyle;
	/**
	 * 该位置最大允许配置的Banner数量
	 */
	@ColumnWidth(20)
	@ExcelProperty("该位置最大允许配置的Banner数量")
	private Integer maxItems;
	/**
	 * 排序，越小越靠前
	 */
	@ColumnWidth(20)
	@ExcelProperty("排序，越小越靠前")
	private Integer sort;
	/**
	 * 备注
	 */
	@ColumnWidth(20)
	@ExcelProperty("备注")
	private String remark;
	/**
	 * 是否删除：0否，1是
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否删除：0否，1是")
	private Integer isDeleted;

}
