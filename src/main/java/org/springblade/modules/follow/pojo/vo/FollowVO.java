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
package org.springblade.modules.follow.pojo.vo;

import org.springblade.modules.follow.pojo.entity.FollowEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 关注表 视图实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FollowVO extends FollowEntity {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 头像
	 */
	private String avatar;
	/**
	 * 用户ID (用于展示列表时的目标用户ID)
	 */
	private Long userId;
	/**
	 * 粉丝数
	 */
	private Long fanCount;
	/**
	 * 是否关注
	 */
	private Boolean isfollow;

}
