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
package org.springblade.modules.newscomment.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 新闻评论表 Excel实体类
 *
 * @author BladeX
 * @since 2026-03-02
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class NewsCommentExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 新闻ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("新闻ID")
	private Long newsId;
	/**
	 * 用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("用户ID")
	private Long userId;
	/**
	 * 用户名
	 */
	@ColumnWidth(20)
	@ExcelProperty("用户名")
	private String username;
	/**
	 * 用户头像
	 */
	@ColumnWidth(20)
	@ExcelProperty("用户头像")
	private String avatar;
	/**
	 * 评论内容
	 */
	@ColumnWidth(20)
	@ExcelProperty("评论内容")
	private String content;
	/**
	 * 父评论ID 0-顶级评论
	 */
	@ColumnWidth(20)
	@ExcelProperty("父评论ID 0-顶级评论")
	private Long parentId;
	/**
	 * 点赞数
	 */
	@ColumnWidth(20)
	@ExcelProperty("点赞数")
	private Integer likeCount;
	/**
	 * 状态 0-待审核 1-已通过 2-已删除
	 */
	@ColumnWidth(20)
	@ExcelProperty("状态 0-待审核 1-已通过 2-已删除")
	private Byte commentStatus;
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
