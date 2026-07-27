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
package org.springblade.modules.comment.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 评论表 实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@TableName("t_comment")
@Schema(description = "Comment对象")
@EqualsAndHashCode(callSuper = true)
public class CommentEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 图片ID
	 */
	@Schema(description = "图片ID")
	private Long mid;
	/**
	 * 用户ID
	 */
	@Schema(description = "用户ID")
	private Long uid;
	/**
	 * 父评论ID
	 */
	@Schema(description = "父评论ID")
	private Long pid;
	/**
	 * 回复ID
	 */
	@Schema(description = "回复ID")
	private Long replyId;
	/**
	 * 评论层级
	 */
	@Schema(description = "评论层级")
	private Integer level;
	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer sort;
	/**
	 * 评论内容
	 */
	@Schema(description = "评论内容")
	private String content;
	/**
	 * 点赞数
	 */
	@Schema(description = "点赞数")
	private Long count;
	/**
	 * 二级评论数
	 */
	@Schema(description = "二级评论数")
	private Long twoNums;
	/**
	 * 回复用户ID
	 */
	@Schema(description = "回复用户ID")
	private Long replyUid;
	/** 审核状态：0待审核 1通过 2未通过 3异常待重试 */
	private Byte auditStatus;
	/** 审核说明 */
	private String auditReason;
	/** 审核完成时间 */
	private Date auditTime;
	/** 审核任务ID */
	private Long auditTaskId;

}
