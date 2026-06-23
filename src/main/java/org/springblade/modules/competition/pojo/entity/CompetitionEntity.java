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
package org.springblade.modules.competition.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 赛事表 实体类
 *
 * @author BladeX
 * @since 2026-03-10
 */
@Data
@TableName("ldqc_competition")
@Schema(description = "Competition对象")
@EqualsAndHashCode(callSuper = true)
public class CompetitionEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 赛事标题
	 */
	@Schema(description = "赛事标题")
	private String title;
	/**
	 * 赛事封面图URL
	 */
	@Schema(description = "赛事封面图URL")
	private String coverImage;
	/**
	 * 开始时间
	 */
	@Schema(description = "开始时间")
	private Date startTime;
	/**
	 * 结束时间
	 */
	@Schema(description = "结束时间")
	private Date endTime;
	/**
	 * 举办地点
	 */
	@Schema(description = "举办地点")
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
	 * 已报名人数
	 */
	@Schema(description = "已报名人数")
	private Integer participantCount;
	/**
	 * 人数上限
	 */
	@Schema(description = "人数上限")
	private Integer maxParticipants;
	/**
	 * 报名费用
	 */
	@Schema(description = "报名费用")
	private BigDecimal price;
	/**
	 * 赛事详情
	 */
	@Schema(description = "赛事详情")
	private String description;

	/**
	 * 人数上限
	 */
	@Schema(description = "赛事状态[1:报名中,2:进行中,3:已结束]")
	private Integer status;

}
