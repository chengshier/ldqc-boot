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
package org.springblade.modules.authtemplatepreset.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 认证模板推荐项(字段/附件) Excel实体类
 *
 * @author BladeX
 * @since 2026-04-09
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class AuthTemplatePresetExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 模板类型[field:字段,attachment:附件]
	 */
	@ColumnWidth(20)
	@ExcelProperty("模板类型[field:字段,attachment:附件]")
	private String templateType;
	/**
	 * 模板名称(中文)
	 */
	@ColumnWidth(20)
	@ExcelProperty("模板名称(中文)")
	private String name;
	/**
	 * 模板编码(英文key)
	 */
	@ColumnWidth(20)
	@ExcelProperty("模板编码(英文key)")
	private String code;
	/**
	 * 控件类型[input,textarea,phone,number,date]
	 */
	@ColumnWidth(20)
	@ExcelProperty("控件类型[input,textarea,phone,number,date]")
	private String componentType;
	/**
	 * 是否必填/必传[0:否,1:是]
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否必填/必传[0:否,1:是]")
	private Byte required;
	/**
	 * 占位提示(字段模板用)
	 */
	@ColumnWidth(20)
	@ExcelProperty("占位提示(字段模板用)")
	private String placeholder;
	/**
	 * 最大上传数量(附件模板用)
	 */
	@ColumnWidth(20)
	@ExcelProperty("最大上传数量(附件模板用)")
	private Integer maxCount;
	/**
	 * 附件提示(附件模板用)
	 */
	@ColumnWidth(20)
	@ExcelProperty("附件提示(附件模板用)")
	private String hint;
	/**
	 * 适用身份(字典key逗号分隔,空表示全部)
	 */
	@ColumnWidth(20)
	@ExcelProperty("适用身份(字典key逗号分隔,空表示全部)")
	private String identityScope;
	/**
	 * 排序
	 */
	@ColumnWidth(20)
	@ExcelProperty("排序")
	private Integer sortOrder;
	/**
	 * 扩展配置
	 */
	@ColumnWidth(20)
	@ExcelProperty("扩展配置")
	private String extJson;
	/**
	 * 是否删除
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否删除")
	private Integer isDeleted;
	/**
	 * 租户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("租户ID")
	private String tenantId;

}
