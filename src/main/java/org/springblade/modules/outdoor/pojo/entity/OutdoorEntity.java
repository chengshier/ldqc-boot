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
package org.springblade.modules.outdoor.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 户外活动表 实体类
 *
 * @author BladeX
 * @since 2026-03-10
 */
@Data
@TableName("ldqc_outdoor")
@Schema(description = "Outdoor对象")
@EqualsAndHashCode(callSuper = true)
public class OutdoorEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 活动标题
	 */
	@Schema(description = "活动标题")
	private String title;
	/**
	 * 封面图URL
	 */
	@Schema(description = "封面图URL")
	private String coverImage;
	/**
	 * 活动截止日期
	 */
	@Schema(description = "活动截止日期")
	private Date endTime;
	/**
	 * 活动地点
	 */
	@Schema(description = "活动地点")
	private String location;

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
	 * 活动难度
	 */
	@Schema(description = "活动难度")
	private String difficulty;
	/**
	 * 组织者用户ID
	 */
	@Schema(description = "组织者用户ID")
	private Long organizerId;
	/**
	 * 点赞/喜欢数
	 */
	@Schema(description = "点赞/喜欢数")
	private Integer likesCount;
	/**
	 * 活动描述
	 */
	@Schema(description = "活动描述")
	private String description;

	/**
	 * 评分
	 */
	@Schema(description = "评分")
	private BigDecimal rating;

	/**
	 * 评论数
	 */
	@Schema(description = "评论数")
	private Integer commentCount;

	/**
	 * 人均价格
	 */
	@Schema(description = "人均价格")
	private BigDecimal price;

	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer sortOrder;

	/**
	 * 排序
	 */
	@Schema(description = "活动状态[1:报名中,2:进行中,3:已结束]")
	private Integer status;

}
