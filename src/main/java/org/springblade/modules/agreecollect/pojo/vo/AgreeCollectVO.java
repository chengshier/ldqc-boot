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
package org.springblade.modules.agreecollect.pojo.vo;

import org.springblade.modules.agreecollect.pojo.entity.AgreeCollectEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import java.util.Date;

/**
 * 点赞收藏表 视图实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgreeCollectVO extends AgreeCollectEntity {
	@Serial
	private static final long serialVersionUID = 1L;


	private Long aid;

	private Long mid;

	private String cover;

	private Long uid;

	private String username;

	private String avatar;

	private String content;

	private String name;

	private Integer count;

	/**
	 * 图片数量
	 */
	private Long imgCount;

	/**
	 * 收藏数量
	 */
	private Long collectionCount;

	// 0是评论，1是图片,2专辑
	private Integer type;

	private Date createDate;

}
