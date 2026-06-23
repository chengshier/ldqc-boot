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
package org.springblade.modules.news.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;
import java.util.List;

/**
 * 新闻表 实体类
 *
 * @author BladeX
 * @since 2026-03-02
 */
@Data
@TableName("n_news")
@Schema(description = "News对象")
@EqualsAndHashCode(callSuper = true)
public class NewsEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 新闻标题
	 */
	@Schema(description = "新闻标题")
	private String title;
	/**
	 * 摘要
	 */
	@Schema(description = "摘要")
	private String abstracts;
	/**
	 * 新闻内容
	 */
	@Schema(description = "新闻内容")
	private String content;
	/**
	 * 封面图片
	 */
	@Schema(description = "封面图片")
	private String cover;
	/**
	 * 作者名称
	 */
	@Schema(description = "作者名称")
	private String username;
	/**
	 * 作者ID
	 */
	@Schema(description = "作者ID")
	private Long authorId;
	/**
	 * 浏览量
	 */
	@Schema(description = "浏览量")
	private Integer viewCount;
	/**
	 * 评论数
	 */
	@Schema(description = "评论数")
	private Integer commentCount;
	/**
	 * 点赞数
	 */
	@Schema(description = "点赞数")
	private Integer agreeCount;
	/**
	 * 分享数
	 */
	@Schema(description = "分享数")
	private Integer shareCount;
	/**
	 * 收藏数
	 */
	@Schema(description = "分享数")
	private Integer collectCount;
	/**
	 * 分类ID
	 */
	@Schema(description = "分类ID")
	private Long categoryId;
	/**
	 * 是否置顶
	 */
	@Schema(description = "是否置顶")
	private Integer isTop;
	/**
	 * 新闻状态
	 */
	@Schema(description = "新闻状态")
	private Integer newsStatus;
	/**
	 * 排序权重
	 */
	@Schema(description = "排序权重")
	private Integer sortOrder;
	/**
	 * 发布时间
	 */
	@Schema(description = "发布时间")
	private String publishTime;
	/**
	 * 多图展示（非数据库字段，用于接收前端轮播图）
	 */
	@TableField(exist = false)
	private List<String> images;
	/**
	 * 分类名称（非数据库字段）
	 */
	@TableField(exist = false)
	private String categoryName;

}
