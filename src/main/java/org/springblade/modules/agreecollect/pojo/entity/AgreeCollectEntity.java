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
package org.springblade.modules.agreecollect.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 点赞收藏表 实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@TableName("t_agree_collect")
@Schema(description = "AgreeCollect对象")
@EqualsAndHashCode(callSuper = true)
public class AgreeCollectEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 点赞的用户
	 */
	@Schema(description = "点赞的用户")
	private Long uid;
	/**
	 * 点赞和收藏的id(可能是图片或者评论)
	 */
	@Schema(description = "点赞和收藏的id(可能是图片或者评论)")
	private Long agreeCollectId;
	/**
	 * 点赞和收藏通知的用户
	 */
	@Schema(description = "点赞和收藏通知的用户")
	private Long agreeCollectUid;
	/**
	 * 类型
	 */
	@Schema(description = "类型")
	private Integer type;

}
