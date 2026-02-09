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
package org.springblade.modules.userthree.excel;


import lombok.Data;

import java.util.Date;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 用户微信登录认证表 Excel实体类
 *
 * @author BladeX
 * @since 2026-02-04
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class UserThreeExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;
	/**
	 * 主用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("主用户ID")
	private Long userId;
	/**
	 * 第三方平台唯一标识(OpenID)
	 */
	@ColumnWidth(20)
	@ExcelProperty("第三方平台唯一标识(OpenID)")
	private String oauthId;
	/**
	 * 第三方平台UnionID(可选)
	 */
	@ColumnWidth(20)
	@ExcelProperty("第三方平台UnionID(可选)")
	private String unionId;
	/**
	 * 来源 (如: wechat_mini, wechat_app)
	 */
	@ColumnWidth(20)
	@ExcelProperty("来源 (如: wechat_mini, wechat_app)")
	private String source;
	/**
	 * 访问令牌(SessionKey)
	 */
	@ColumnWidth(20)
	@ExcelProperty("访问令牌(SessionKey)")
	private String accessToken;
	/**
	 * 第三方头像
	 */
	@ColumnWidth(20)
	@ExcelProperty("第三方头像")
	private String avatar;
	/**
	 * 第三方昵称
	 */
	@ColumnWidth(20)
	@ExcelProperty("第三方昵称")
	private String username;
	/**
	 * 租户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("租户ID")
	private String tenantId;
	/**
	 * 是否已删除
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否已删除")
	private Integer isDeleted;

}
