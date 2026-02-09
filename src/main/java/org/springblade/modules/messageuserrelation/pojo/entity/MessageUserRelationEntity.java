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
package org.springblade.modules.messageuserrelation.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 消息用户关系表 实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@TableName("t_message_user_relation")
@Schema(description = "MessageUserRelation对象")
@EqualsAndHashCode(callSuper = true)
public class MessageUserRelationEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 发送者ID
	 */
	@Schema(description = "发送者ID")
	private Long sendId;
	/**
	 * 接收者ID
	 */
	@Schema(description = "接收者ID")
	private Long acceptId;
	/**
	 * 未读消息数
	 */
	@Schema(description = "未读消息数")
	private Integer count;
	/**
	 * 最后一条消息
	 */
	@Schema(description = "最后一条消息")
	private String content;

}
