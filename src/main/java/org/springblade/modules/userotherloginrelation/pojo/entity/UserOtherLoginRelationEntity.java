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
package org.springblade.modules.userotherloginrelation.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 用户第三方登录关系表 实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@TableName("t_user_other_login_relation")
@Schema(description = "UserOtherLoginRelation对象")
@EqualsAndHashCode(callSuper = true)
public class UserOtherLoginRelationEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	@Schema(description = "用户ID")
	private Long uid;
	/**
	 * 第三方用户ID
	 */
	@Schema(description = "第三方用户ID")
	private String otherUserId;
	/**
	 * 第三方用户名
	 */
	@Schema(description = "第三方用户名")
	private String otherUsername;
	/**
	 * 第三方头像
	 */
	@Schema(description = "第三方头像")
	private String otherAvatar;
	/**
	 * 第三方token
	 */
	@Schema(description = "第三方token")
	private String otherToken;

}
