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
package org.springblade.modules.trainingteacher.excel;


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
 * 培训教练表 Excel实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class TrainingTeacherExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 所属机构ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("所属机构ID")
	private Long orgId;
	/**
	 * 教练姓名
	 */
	@ColumnWidth(20)
	@ExcelProperty("教练姓名")
	private String name;
	/**
	 * 头像URL
	 */
	@ColumnWidth(20)
	@ExcelProperty("头像URL")
	private String avatar;
	/**
	 * 头衔/职称
	 */
	@ColumnWidth(20)
	@ExcelProperty("头衔/职称")
	private String title;
	/**
	 * 标签,逗号分隔
	 */
	@ColumnWidth(20)
	@ExcelProperty("标签,逗号分隔")
	private String tags;
	/**
	 * 简介
	 */
	@ColumnWidth(20)
	@ExcelProperty("简介")
	private String intro;
	/**
	 * 从业年限
	 */
	@ColumnWidth(20)
	@ExcelProperty("从业年限")
	private Integer experienceYears;
	/**
	 * 评分
	 */
	@ColumnWidth(20)
	@ExcelProperty("评分")
	private BigDecimal rating;
	/**
	 * 是否推荐
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否推荐")
	private Integer isRecommended;
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
