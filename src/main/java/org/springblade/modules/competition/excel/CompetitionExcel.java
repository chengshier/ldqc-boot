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
package org.springblade.modules.competition.excel;


import lombok.Data;

import java.util.Date;
import java.math.BigDecimal;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 赛事表 Excel实体类
 *
 * @author BladeX
 * @since 2026-03-10
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class CompetitionExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 赛事标题
	 */
	@ColumnWidth(20)
	@ExcelProperty("赛事标题")
	private String title;
	/**
	 * 赛事封面图URL
	 */
	@ColumnWidth(20)
	@ExcelProperty("赛事封面图URL")
	private String coverImage;
	/**
	 * 开始时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("开始时间")
	private Date startTime;
	/**
	 * 结束时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("结束时间")
	private Date endTime;
	/**
	 * 举办地点
	 */
	@ColumnWidth(20)
	@ExcelProperty("举办地点")
	private String location;
	/**
	 * 已报名人数
	 */
	@ColumnWidth(20)
	@ExcelProperty("已报名人数")
	private Integer participantCount;
	/**
	 * 人数上限
	 */
	@ColumnWidth(20)
	@ExcelProperty("人数上限")
	private Integer maxParticipants;
	/**
	 * 报名费用
	 */
	@ColumnWidth(20)
	@ExcelProperty("报名费用")
	private BigDecimal price;
	/**
	 * 赛事详情
	 */
	@ColumnWidth(20)
	@ExcelProperty("赛事详情")
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
