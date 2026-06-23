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
package org.springblade.modules.talentpost.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 达人动态表 实体类
 *
 * @author BladeX
 * @since 2026-03-11
 */
@Data
@TableName("ldqc_talent_post")
@Schema(description = "TalentPost对象")
@EqualsAndHashCode(callSuper = true)
public class TalentPostEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 关联用户ID
	 */
	@Schema(description = "关联用户ID")
	private Long userId;
	/**
	 * 动态标题
	 */
	@Schema(description = "动态标题")
	private String title;
	/**
	 * 动态文案
	 */
	@Schema(description = "动态文案")
	private String content;
	/**
	 * 封面图URL
	 */
	@Schema(description = "封面图URL")
	private String coverImage;
	/**
	 * 媒体封面/首帧地址
	 */
	@Schema(description = "媒体封面/首帧地址")
	private String posterUrl;
	/**
	 * 媒体URL
	 */
	@Schema(description = "媒体URL")
	private String mediaUrl;
	/**
	 * 媒体类型[image,video]
	 */
	@Schema(description = "媒体类型[image,video]")
	private String mediaType;
	/**
	 * 媒体时长，单位秒
	 */
	@Schema(description = "媒体时长，单位秒")
	private Integer duration;
	/**
	 * 文件大小，单位字节
	 */
	@Schema(description = "文件大小，单位字节")
	private Long fileSize;
	/**
	 * 媒体宽度
	 */
	@Schema(description = "媒体宽度")
	private Integer width;
	/**
	 * 媒体高度
	 */
	@Schema(description = "媒体高度")
	private Integer height;
	/**
	 * 点赞数
	 */
	@Schema(description = "点赞数")
	private Integer agreeCount;
	/**
	 * 评论数
	 */
	@Schema(description = "评论数")
	private Integer commentCount;
	/**
	 * 分享数
	 */
	@Schema(description = "分享数")
	private Integer shareCount;
	/**
	 * 浏览数
	 */
	@Schema(description = "浏览数")
	private Integer viewCount;
	/**
	 * 动态标签
	 */
	@Schema(description = "动态标签")
	private String postTag;

}
