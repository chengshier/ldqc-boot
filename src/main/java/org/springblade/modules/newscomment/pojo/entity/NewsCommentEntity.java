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
package org.springblade.modules.newscomment.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 新闻评论表 实体类
 *
 * @author BladeX
 * @since 2026-03-02
 */
@Data
@TableName("n_news_comment")
@Schema(description = "NewsComment对象")
@EqualsAndHashCode(callSuper = true)
public class NewsCommentEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 新闻ID
	 */
	@Schema(description = "新闻ID")
	private Long newsId;
	/**
	 * 用户ID
	 */
	@Schema(description = "用户ID")
	private Long userId;
	/**
	 * 用户名
	 */
	@Schema(description = "用户名")
	private String username;
	/**
	 * 用户头像
	 */
	@Schema(description = "用户头像")
	private String avatar;
	/**
	 * 评论内容
	 */
	@Schema(description = "评论内容")
	private String content;
	/**
	 * 父评论ID 0-顶级评论
	 */
	@Schema(description = "父评论ID 0-顶级评论")
	private Long parentId;
	/**
	 * 点赞数
	 */
	@Schema(description = "点赞数")
	private Integer likeCount;
	/**
	 * 状态 0-待审核 1-已通过 2-已删除
	 */
	@Schema(description = "状态 0-待审核 1-已通过 2-已删除")
	private Byte commentStatus;

}
