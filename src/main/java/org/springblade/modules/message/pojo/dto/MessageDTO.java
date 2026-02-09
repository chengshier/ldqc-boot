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
package org.springblade.modules.message.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springblade.modules.message.pojo.entity.MessageEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 消息表 数据传输对象实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageDTO extends MessageEntity {
	@Serial
	private static final long serialVersionUID = 1L;


	@Schema(description = "id")
	private Long id;

	@Schema(description = "发送方的id")
	private Long sendId;

	@Schema(description = "接收方的id")
	private Long acceptId;

	@Schema(description = "内容")
	private String content;

	@Schema(description = "时间")
	private String time;

}
