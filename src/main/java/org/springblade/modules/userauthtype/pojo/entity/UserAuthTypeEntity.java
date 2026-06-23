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
package org.springblade.modules.userauthtype.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 用户认证类型表 实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@TableName("ldqc_user_auth_type")
@Schema(description = "UserAuthType对象")
@EqualsAndHashCode(callSuper = true)
public class UserAuthTypeEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 身份编码
	 */
	@Schema(description = "身份编码")
	private String code;
	/**
	 * 身份名称
	 */
	@Schema(description = "身份名称")
	private String name;
	/**
	 * 图标
	 */
	@Schema(description = "图标")
	private String icon;
	/**
	 * 封面图
	 */
	@Schema(description = "封面图")
	private String coverImage;
	/**
	 * 说明
	 */
	@Schema(description = "说明")
	private String description;
	/**
	 * 表单配置JSON
	 */
	@Schema(description = "表单配置JSON")
	private String formSchema;
	/**
	 * 附件配置JSON
	 */
	@Schema(description = "附件配置JSON")
	private String attachmentSchema;
	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer sortOrder;

}
