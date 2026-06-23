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
package org.springblade.modules.competitionsignup.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 赛事报名表 Excel实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class CompetitionSignupExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 赛事ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("赛事ID")
	private Long competitionId;
	/**
	 * 报名用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("报名用户ID")
	private Long userId;
	/**
	 * 报名姓名
	 */
	@ColumnWidth(20)
	@ExcelProperty("报名姓名")
	private String signupName;
	/**
	 * 联系电话
	 */
	@ColumnWidth(20)
	@ExcelProperty("联系电话")
	private String phone;
	/**
	 * 身份证号
	 */
	@ColumnWidth(20)
	@ExcelProperty("身份证号")
	private String idCard;
	/**
	 * 队伍名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("队伍名称")
	private String teamName;
	/**
	 * 报名人数
	 */
	@ColumnWidth(20)
	@ExcelProperty("报名人数")
	private Integer numPeople;
	/**
	 * 支付状态[0:未支付,1:已支付,2:退款]
	 */
	@ColumnWidth(20)
	@ExcelProperty("支付状态[0:未支付,1:已支付,2:退款]")
	private Integer payStatus;
	/**
	 * 报名时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("报名时间")
	private Date signupTime;
	/**
	 * 备注
	 */
	@ColumnWidth(20)
	@ExcelProperty("备注")
	private String remark;
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
