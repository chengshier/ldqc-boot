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
package org.springblade.modules.bannerposition.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 宣传Banner位置表 实体类
 *
 * @author BladeX
 * @since 2026-07-06
 */
@Data
@TableName("ldqc_banner_position")
@Schema(description = "BannerPosition对象")
@EqualsAndHashCode(callSuper = true)
public class BannerPositionEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 位置编码，如 home_training
	 */
	@Schema(description = "位置编码，如 home_training")
	private String code;
	/**
	 * 位置名称
	 */
	@Schema(description = "位置名称")
	private String name;
	/**
	 * 终端类型，如 miniapp/h5/all
	 */
	@Schema(description = "终端类型，如 miniapp/h5/all")
	private String terminal;
	/**
	 * 页面编码，如 home_index
	 */
	@Schema(description = "页面编码，如 home_index")
	private String pageCode;
	/**
	 * 频道编码，如 training/venue/outdoor
	 */
	@Schema(description = "频道编码，如 training/venue/outdoor")
	private String channelCode;
	/**
	 * 展示样式，如 image_only/image_text_bottom_left/image_text_center_badge
	 */
	@Schema(description = "展示样式，如 image_only/image_text_bottom_left/image_text_center_badge")
	private String displayStyle;
	/**
	 * 该位置最大允许配置的Banner数量
	 */
	@Schema(description = "该位置最大允许配置的Banner数量")
	private Integer maxItems;
	/**
	 * 排序，越小越靠前
	 */
	@Schema(description = "排序，越小越靠前")
	private Integer sort;
	/**
	 * 备注
	 */
	@Schema(description = "备注")
	private String remark;

}
