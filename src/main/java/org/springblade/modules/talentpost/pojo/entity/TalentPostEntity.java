/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 */
package org.springblade.modules.talentpost.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

/**
 * 达人动态表实体。
 */
@Data
@TableName("ldqc_talent_post")
@Schema(description = "TalentPost对象")
@EqualsAndHashCode(callSuper = true)
public class TalentPostEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/** 来源社区内容ID，用于审核通过后的幂等同步 */
	@Schema(description = "来源社区内容ID")
	private Long sourceContentId;
	/** 关联用户ID */
	@Schema(description = "关联用户ID")
	private Long userId;
	/** 动态标题 */
	@Schema(description = "动态标题")
	private String title;
	/** 动态文案 */
	@Schema(description = "动态文案")
	private String content;
	/** 封面图URL */
	@Schema(description = "封面图URL")
	private String coverImage;
	/** 媒体封面/首帧地址 */
	@Schema(description = "媒体封面/首帧地址")
	private String posterUrl;
	/** 媒体URL */
	@Schema(description = "媒体URL")
	private String mediaUrl;
	/** 媒体类型[image,video] */
	@Schema(description = "媒体类型[image,video]")
	private String mediaType;
	/** 媒体时长，单位秒 */
	@Schema(description = "媒体时长，单位秒")
	private Integer duration;
	/** 文件大小，单位字节 */
	@Schema(description = "文件大小，单位字节")
	private Long fileSize;
	/** 媒体宽度 */
	@Schema(description = "媒体宽度")
	private Integer width;
	/** 媒体高度 */
	@Schema(description = "媒体高度")
	private Integer height;
	/** 点赞数 */
	@Schema(description = "点赞数")
	private Integer agreeCount;
	/** 评论数 */
	@Schema(description = "评论数")
	private Integer commentCount;
	/** 分享数 */
	@Schema(description = "分享数")
	private Integer shareCount;
	/** 浏览数 */
	@Schema(description = "浏览数")
	private Integer viewCount;
	/** 动态标签 */
	@Schema(description = "动态标签")
	private String postTag;
}
