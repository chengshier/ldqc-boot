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
package org.springblade.modules.comment.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 评论表 Excel实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class CommentExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 租户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("租户ID")
	private String tenantId;
	/**
	 * 图片ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("图片ID")
	private Long mid;
	/**
	 * 用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("用户ID")
	private Long uid;
	/**
	 * 父评论ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("父评论ID")
	private Long pid;
	/**
	 * 回复ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("回复ID")
	private Long replyId;
	/**
	 * 评论层级
	 */
	@ColumnWidth(20)
	@ExcelProperty("评论层级")
	private Integer level;
	/**
	 * 排序
	 */
	@ColumnWidth(20)
	@ExcelProperty("排序")
	private Integer sort;
	/**
	 * 评论内容
	 */
	@ColumnWidth(20)
	@ExcelProperty("评论内容")
	private String content;
	/**
	 * 点赞数
	 */
	@ColumnWidth(20)
	@ExcelProperty("点赞数")
	private Long count;
	/**
	 * 二级评论数
	 */
	@ColumnWidth(20)
	@ExcelProperty("二级评论数")
	private Long twoNums;
	/**
	 * 回复用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("回复用户ID")
	private Long replyUid;
	/**
	 * 是否已删除
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否已删除")
	private Integer isDeleted;

}
