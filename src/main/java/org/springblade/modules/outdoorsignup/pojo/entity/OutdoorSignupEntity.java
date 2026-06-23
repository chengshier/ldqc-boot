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
package org.springblade.modules.outdoorsignup.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 户外报名表 实体类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Data
@TableName("ldqc_outdoor_signup")
@Schema(description = "OutdoorSignup对象")
@EqualsAndHashCode(callSuper = true)
public class OutdoorSignupEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 户外活动ID
	 */
	@Schema(description = "户外活动ID")
	private Long outdoorId;
	/**
	 * 报名用户ID
	 */
	@Schema(description = "报名用户ID")
	private Long userId;
	/**
	 * 联系人姓名
	 */
	@Schema(description = "联系人姓名")
	private String contactName;
	/**
	 * 联系电话
	 */
	@Schema(description = "联系电话")
	private String phone;
	/**
	 * 人数
	 */
	@Schema(description = "人数")
	private Integer numPeople;
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
