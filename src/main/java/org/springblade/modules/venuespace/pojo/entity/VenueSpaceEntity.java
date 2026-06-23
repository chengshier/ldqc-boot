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
package org.springblade.modules.venuespace.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 场馆场地表 实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@TableName("ldqc_venue_space")
@Schema(description = "VenueSpace对象")
@EqualsAndHashCode(callSuper = true)
public class VenueSpaceEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 场馆ID
	 */
	@Schema(description = "场馆ID")
	private Long venueId;
	/**
	 * 场地名称
	 */
	@Schema(description = "场地名称")
	private String spaceName;
	/**
	 * 场地类型
	 */
	@Schema(description = "场地类型")
	private String spaceType;
	/**
	 * 价格
	 */
	@Schema(description = "价格")
	private BigDecimal price;
	/**
	 * 容量
	 */
	@Schema(description = "容量")
	private Integer capacity;
	/**
	 * 图集,逗号分隔
	 */
	@Schema(description = "图集,逗号分隔")
	private String images;
	/**
	 * 场地介绍
	 */
	@Schema(description = "场地介绍")
	private String description;
	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer sortOrder;

}
