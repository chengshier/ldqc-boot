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
package org.springblade.modules.venue.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 体育场馆表 实体类
 *
 * @author BladeX
 * @since 2026-03-10
 */
@Data
@TableName("ldqc_venue")
@Schema(description = "Venue对象")
@EqualsAndHashCode(callSuper = true)
public class VenueEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 场馆名称
	 */
	@Schema(description = "场馆名称")
	private String name;
	/**
	 * 场馆主图URL
	 */
	@Schema(description = "场馆主图URL")
	private String coverImage;
	/**
	 * 图集,逗号分隔
	 */
	@Schema(description = "图集,逗号分隔")
	private String images;
	/**
	 * 详细地址
	 */
	@Schema(description = "详细地址")
	private String address;
	/**
	 * 经度
	 */
	@Schema(description = "经度")
	private BigDecimal longitude;
	/**
	 * 纬度
	 */
	@Schema(description = "纬度")
	private BigDecimal latitude;
	/**
	 * 评分
	 */
	@Schema(description = "评分")
	private BigDecimal rating;
	/**
	 * 标签,逗号分隔
	 */
	@Schema(description = "标签,逗号分隔")
	private String tags;
	/**
	 * 营业时间
	 */
	@Schema(description = "营业时间")
	private String businessHours;
	/**
	 * 联系电话
	 */
	@Schema(description = "联系电话")
	private String phone;
	/**
	 * 场馆介绍
	 */
	@Schema(description = "场馆介绍")
	private String description;

	/**
	 * 场馆类型ID
	 */
	@Schema(description = "场馆类型ID")
	private Long typeId;
	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private String sortOrder;
}
