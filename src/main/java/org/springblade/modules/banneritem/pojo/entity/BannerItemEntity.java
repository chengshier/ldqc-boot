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
package org.springblade.modules.banneritem.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 宣传Banner内容表 实体类
 *
 * @author BladeX
 * @since 2026-07-06
 */
@Data
@TableName("ldqc_banner_item")
@Schema(description = "BannerItem对象")
@EqualsAndHashCode(callSuper = true)
public class BannerItemEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 位置ID，关联ldqc_banner_position.id
	 */
	@Schema(description = "位置ID，关联ldqc_banner_position.id")
	private Long positionId;
	/**
	 * 主标题
	 */
	@Schema(description = "主标题")
	private String title;
	/**
	 * 副标题
	 */
	@Schema(description = "副标题")
	private String subtitle;
	/**
	 * 描述文案
	 */
	@Schema(description = "描述文案")
	private String description;
	/**
	 * 标签文案，如 推荐/报名中
	 */
	@Schema(description = "标签文案，如 推荐/报名中")
	private String tagText;
	/**
	 * Banner图片URL
	 */
	@Schema(description = "Banner图片URL")
	private String imageUrl;
	/**
	 * 跳转类型，如 none/miniapp_path/h5_url
	 */
	@Schema(description = "跳转类型，如 none/miniapp_path/h5_url")
	private String jumpType;
	/**
	 * 跳转值，如小程序页面路径或H5地址
	 */
	@Schema(description = "跳转值，如小程序页面路径或H5地址")
	private String jumpValue;
	/**
	 * 扩展JSON，用于少量个性化字段，如badgeTop/badgeBottom
	 */
	@Schema(description = "扩展JSON，用于少量个性化字段，如badgeTop/badgeBottom")
	private String extJson;
	/**
	 * 排序，越小越靠前
	 */
	@Schema(description = "排序，越小越靠前")
	private Integer sort;
	/**
	 * 生效开始时间，为空表示立即生效
	 */
	@Schema(description = "生效开始时间，为空表示立即生效")
	private Date publishStartTime;
	/**
	 * 生效结束时间，为空表示长期有效
	 */
	@Schema(description = "生效结束时间，为空表示长期有效")
	private Date publishEndTime;
	/**
	 * 备注
	 */
	@Schema(description = "备注")
	private String remark;

}
