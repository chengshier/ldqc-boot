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
package org.springblade.modules.comment.pojo.vo;

import org.springblade.modules.comment.pojo.entity.CommentEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 评论表 视图实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends CommentEntity {
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
	 * 回复用户名
	 */
	private String replyName;
	/**
	 * 回复内容
	 */
	private String replyContent;
	/**
	 * 是否点赞
	 */
	private Boolean isAgree;

	/**
	 * 子评论 (用于列表展示一条子评论)
	 */
	private CommentVO childComment;

	private String cover;

}
