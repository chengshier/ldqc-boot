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
package org.springblade.modules.training.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 培训课程表 实体类
 *
 * @author BladeX
 * @since 2026-03-10
 */
@Data
@TableName("ldqc_training")
@Schema(description = "Training对象")
@EqualsAndHashCode(callSuper = true)
public class TrainingEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 课程标题
	 */
	@Schema(description = "课程标题")
	private String title;
	/**
	 * 课程封面图URL
	 */
	@Schema(description = "课程封面图URL")
	private String coverImage;
	/**
	 * 教练姓名
	 */
	@Schema(description = "教练姓名")
	private String instructorName;
	/**
	 * 课程价格
	 */
	@Schema(description = "课程价格")
	private BigDecimal price;
	/**
	 * 课程时长(分钟)
	 */
	@Schema(description = "课程时长(分钟)")
	private Integer duration;
	/**
	 * 上课地点
	 */
	@Schema(description = "上课地点")
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
	 * 课程分类
	 */
	@Schema(description = "课程分类")
	private String category;
	/**
	 * 课程介绍
	 */
	@Schema(description = "课程介绍")
	private String description;

	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer sortOrder;

	/**
	 * 排序
	 */
	@Schema(description = "课程状态[1:启用,0:禁用]")
	private Integer status;


	/**
	 * 所属机构ID
	 */
	@Schema(description = "所属机构ID")
	private Long orgId;

	/**
	 * 主讲教练ID
	 */
	@Schema(description = "主讲教练ID")
	private Long teacherId;


	/**
	 * 课程类型[体验课,正式课,训练营]
	 */
	@Schema(description = "课程类型[体验课,正式课,训练营]")
	private String courseType;
}
