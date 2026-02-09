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
package org.springblade.modules.userthree.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 用户微信登录认证表 实体类
 *
 * @author BladeX
 * @since 2026-02-04
 */
@Data
@TableName("blade_user_three")
@Schema(description = "UserThree对象")
@EqualsAndHashCode(callSuper = true)
public class UserThreeEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主用户ID
	 */
	@Schema(description = "主用户ID")
	private Long userId;
	/**
	 * 第三方平台唯一标识(OpenID)
	 */
	@Schema(description = "第三方平台唯一标识(OpenID)")
	private String oauthId;
	/**
	 * 第三方平台UnionID(可选)
	 */
	@Schema(description = "第三方平台UnionID(可选)")
	private String unionId;
	/**
	 * 来源 (如: wechat_mini, wechat_app)
	 */
	@Schema(description = "来源 (如: wechat_mini, wechat_app)")
	private String source;
	/**
	 * 访问令牌(SessionKey)
	 */
	@Schema(description = "访问令牌(SessionKey)")
	private String accessToken;
	/**
	 * 第三方头像
	 */
	@Schema(description = "第三方头像")
	private String avatar;
	/**
	 * 第三方昵称
	 */
	@Schema(description = "第三方昵称")
	private String username;

}
