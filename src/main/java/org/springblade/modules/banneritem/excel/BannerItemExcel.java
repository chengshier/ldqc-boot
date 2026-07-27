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
package org.springblade.modules.banneritem.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 宣传Banner内容表 Excel实体类
 *
 * @author BladeX
 * @since 2026-07-06
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class BannerItemExcel implements Serializable {

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
	 * 位置ID，关联ldqc_banner_position.id
	 */
	@ColumnWidth(20)
	@ExcelProperty("位置ID，关联ldqc_banner_position.id")
	private Long positionId;
	/**
	 * 主标题
	 */
	@ColumnWidth(20)
	@ExcelProperty("主标题")
	private String title;
	/**
	 * 副标题
	 */
	@ColumnWidth(20)
	@ExcelProperty("副标题")
	private String subtitle;
	/**
	 * 描述文案
	 */
	@ColumnWidth(20)
	@ExcelProperty("描述文案")
	private String description;
	/**
	 * 标签文案，如 推荐/报名中
	 */
	@ColumnWidth(20)
	@ExcelProperty("标签文案，如 推荐/报名中")
	private String tagText;
	/**
	 * Banner图片URL
	 */
	@ColumnWidth(20)
	@ExcelProperty("Banner图片URL")
	private String imageUrl;
	/**
	 * 跳转类型，如 none/miniapp_path/h5_url
	 */
	@ColumnWidth(20)
	@ExcelProperty("跳转类型，如 none/miniapp_path/h5_url")
	private String jumpType;
	/**
	 * 跳转值，如小程序页面路径或H5地址
	 */
	@ColumnWidth(20)
	@ExcelProperty("跳转值，如小程序页面路径或H5地址")
	private String jumpValue;
	/**
	 * 扩展JSON，用于少量个性化字段，如badgeTop/badgeBottom
	 */
	@ColumnWidth(20)
	@ExcelProperty("扩展JSON，用于少量个性化字段，如badgeTop/badgeBottom")
	private String extJson;
	/**
	 * 排序，越小越靠前
	 */
	@ColumnWidth(20)
	@ExcelProperty("排序，越小越靠前")
	private Integer sort;
	/**
	 * 生效开始时间，为空表示立即生效
	 */
	@ColumnWidth(20)
	@ExcelProperty("生效开始时间，为空表示立即生效")
	private Date publishStartTime;
	/**
	 * 生效结束时间，为空表示长期有效
	 */
	@ColumnWidth(20)
	@ExcelProperty("生效结束时间，为空表示长期有效")
	private Date publishEndTime;
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
