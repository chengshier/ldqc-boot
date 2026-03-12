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
package org.springblade.modules.news.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 新闻表 Excel实体类
 *
 * @author BladeX
 * @since 2026-03-02
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class NewsExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 新闻标题
	 */
	@ColumnWidth(20)
	@ExcelProperty("新闻标题")
	private String title;
	/**
	 * 新闻内容
	 */
	@ColumnWidth(20)
	@ExcelProperty("新闻内容")
	private String content;
	/**
	 * 封面图片
	 */
	@ColumnWidth(20)
	@ExcelProperty("封面图片")
	private String cover;
	/**
	 * 作者名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("作者名称")
	private String username;
	/**
	 * 作者ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("作者ID")
	private Long authorId;
	/**
	 * 浏览量
	 */
	@ColumnWidth(20)
	@ExcelProperty("浏览量")
	private Integer viewCount;
	/**
	 * 评论数
	 */
	@ColumnWidth(20)
	@ExcelProperty("评论数")
	private Integer commentCount;
	/**
	 * 点赞数
	 */
	@ColumnWidth(20)
	@ExcelProperty("点赞数")
	private Integer agreeCount;
	/**
	 * 分享数
	 */
	@ColumnWidth(20)
	@ExcelProperty("分享数")
	private Integer shareCount;
	/**
	 * 分类ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("分类ID")
	private Long categoryId;
	/**
	 * 是否置顶
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否置顶")
	private Integer isTop;
	/**
	 * 新闻状态
	 */
	@ColumnWidth(20)
	@ExcelProperty("新闻状态")
	private Integer newsStatus;
	/**
	 * 排序权重
	 */
	@ColumnWidth(20)
	@ExcelProperty("排序权重")
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
	/**
	 * 发布时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("发布时间")
	private Date publishTime;

}
