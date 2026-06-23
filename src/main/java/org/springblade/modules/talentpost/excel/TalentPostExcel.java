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
package org.springblade.modules.talentpost.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 达人动态表 Excel实体类
 *
 * @author BladeX
 * @since 2026-03-11
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class TalentPostExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 关联用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("关联用户ID")
	private Long userId;
	/**
	 * 动态标题
	 */
	@ColumnWidth(20)
	@ExcelProperty("动态标题")
	private String title;
	/**
	 * 动态文案
	 */
	@ColumnWidth(20)
	@ExcelProperty("动态文案")
	private String content;
	/**
	 * 封面图URL
	 */
	@ColumnWidth(20)
	@ExcelProperty("封面图URL")
	private String coverImage;
	/**
	 * 媒体封面/首帧地址
	 */
	@ColumnWidth(20)
	@ExcelProperty("媒体封面/首帧地址")
	private String posterUrl;
	/**
	 * 媒体URL
	 */
	@ColumnWidth(20)
	@ExcelProperty("媒体URL")
	private String mediaUrl;
	/**
	 * 媒体类型[image,video]
	 */
	@ColumnWidth(20)
	@ExcelProperty("媒体类型[image,video]")
	private String mediaType;
	/**
	 * 媒体时长
	 */
	@ColumnWidth(20)
	@ExcelProperty("媒体时长")
	private Integer duration;
	/**
	 * 文件大小
	 */
	@ColumnWidth(20)
	@ExcelProperty("文件大小")
	private Long fileSize;
	/**
	 * 媒体宽度
	 */
	@ColumnWidth(20)
	@ExcelProperty("媒体宽度")
	private Integer width;
	/**
	 * 媒体高度
	 */
	@ColumnWidth(20)
	@ExcelProperty("媒体高度")
	private Integer height;
	/**
	 * 点赞数
	 */
	@ColumnWidth(20)
	@ExcelProperty("点赞数")
	private Integer agreeCount;
	/**
	 * 评论数
	 */
	@ColumnWidth(20)
	@ExcelProperty("评论数")
	private Integer commentCount;
	/**
	 * 分享数
	 */
	@ColumnWidth(20)
	@ExcelProperty("分享数")
	private Integer shareCount;
	/**
	 * 浏览数
	 */
	@ColumnWidth(20)
	@ExcelProperty("浏览数")
	private Integer viewCount;
	/**
	 * 动态标签
	 */
	@ColumnWidth(20)
	@ExcelProperty("动态标签")
	private String postTag;
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
