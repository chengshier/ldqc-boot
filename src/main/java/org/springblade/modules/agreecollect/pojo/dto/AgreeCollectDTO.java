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
package org.springblade.modules.agreecollect.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springblade.modules.agreecollect.pojo.entity.AgreeCollectEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;

/**
 * 点赞收藏表 数据传输对象实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgreeCollectDTO extends AgreeCollectEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "当前点赞的用户id")
	@NotNull(message = "uid不能为空")
	private Long uid;

	@Schema(description = "点赞的类型id")
	@NotNull(message = "点赞id不能为空")
	private Long agreeCollectId;

	@Schema(description = "点赞图片或评论发布的用户id")
	@NotNull(message = "给他人点赞id不能为空")
	private Long agreeCollectUid;

	@Schema(description = "0代表点赞评论，1代表点赞图片,2代表收藏图片,3是收藏专辑")
	private Integer type;

}
