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
package org.springblade.modules.trainingteacher.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 培训教练表 实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@TableName("ldqc_training_teacher")
@Schema(description = "TrainingTeacher对象")
@EqualsAndHashCode(callSuper = true)
public class TrainingTeacherEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 所属机构ID
	 */
	@Schema(description = "所属机构ID")
	private Long orgId;
	/**
	 * 教练姓名
	 */
	@Schema(description = "教练姓名")
	private String name;
	/**
	 * 头像URL
	 */
	@Schema(description = "头像URL")
	private String avatar;
	/**
	 * 头衔/职称
	 */
	@Schema(description = "头衔/职称")
	private String title;
	/**
	 * 标签,逗号分隔
	 */
	@Schema(description = "标签,逗号分隔")
	private String tags;
	/**
	 * 简介
	 */
	@Schema(description = "简介")
	private String intro;
	/**
	 * 从业年限
	 */
	@Schema(description = "从业年限")
	private Integer experienceYears;
	/**
	 * 评分
	 */
	@Schema(description = "评分")
	private BigDecimal rating;
	/**
	 * 是否推荐
	 */
	@Schema(description = "是否推荐")
	private Integer isRecommended;
	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer sortOrder;

}
