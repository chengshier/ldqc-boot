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
package org.springblade.modules.agreecollect.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 点赞收藏表 Excel实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class AgreeCollectExcel implements Serializable {

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
	 * 点赞的用户
	 */
	@ColumnWidth(20)
	@ExcelProperty("点赞的用户")
	private Long uid;
	/**
	 * 点赞和收藏的id(可能是图片或者评论)
	 */
	@ColumnWidth(20)
	@ExcelProperty("点赞和收藏的id(可能是图片或者评论)")
	private Long agreeCollectId;
	/**
	 * 点赞和收藏通知的用户
	 */
	@ColumnWidth(20)
	@ExcelProperty("点赞和收藏通知的用户")
	private Long agreeCollectUid;
	/**
	 * 类型
	 */
	@ColumnWidth(20)
	@ExcelProperty("类型")
	private Integer type;
	/**
	 * 是否已删除
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否已删除")
	private Integer isDeleted;

}
