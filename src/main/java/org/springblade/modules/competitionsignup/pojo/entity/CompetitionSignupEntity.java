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
package org.springblade.modules.competitionsignup.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 赛事报名表 实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@TableName("ldqc_competition_signup")
@Schema(description = "CompetitionSignup对象")
@EqualsAndHashCode(callSuper = true)
public class CompetitionSignupEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 赛事ID
	 */
	@Schema(description = "赛事ID")
	private Long competitionId;
	/**
	 * 报名用户ID
	 */
	@Schema(description = "报名用户ID")
	private Long userId;
	/**
	 * 报名姓名
	 */
	@Schema(description = "报名姓名")
	private String signupName;
	/**
	 * 联系电话
	 */
	@Schema(description = "联系电话")
	private String phone;
	/**
	 * 身份证号
	 */
	@Schema(description = "身份证号")
	private String idCard;
	/**
	 * 队伍名称
	 */
	@Schema(description = "队伍名称")
	private String teamName;
	/**
	 * 报名人数
	 */
	@Schema(description = "报名人数")
	private Integer numPeople;
	/**
	 * 支付状态[0:未支付,1:已支付,2:退款]
	 */
	@Schema(description = "支付状态[0:未支付,1:已支付,2:退款]")
	private Integer payStatus;
	/**
	 * 报名时间
	 */
	@Schema(description = "报名时间")
	private Date signupTime;
	/**
	 * 备注
	 */
	@Schema(description = "备注")
	private String remark;

}
