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
package org.springblade.modules.outdoorsignup.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 户外报名表 Excel实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class OutdoorSignupExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 户外活动ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("户外活动ID")
	private Long outdoorId;
	/**
	 * 报名用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("报名用户ID")
	private Long userId;
	/**
	 * 联系人姓名
	 */
	@ColumnWidth(20)
	@ExcelProperty("联系人姓名")
	private String contactName;
	/**
	 * 联系电话
	 */
	@ColumnWidth(20)
	@ExcelProperty("联系电话")
	private String phone;
	/**
	 * 人数
	 */
	@ColumnWidth(20)
	@ExcelProperty("人数")
	private Integer numPeople;
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
