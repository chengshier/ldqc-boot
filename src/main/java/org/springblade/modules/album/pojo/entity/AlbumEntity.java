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
package org.springblade.modules.album.pojo.entity;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 相册表 实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@TableName("t_album")
@Schema(description = "Album对象")
@EqualsAndHashCode(callSuper = true)
public class AlbumEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 相册名称
	 */
	@Schema(description = "相册名称")
	private String name;
	/**
	 * 用户ID
	 */
	@Schema(description = "用户ID")
	private Long uid;
	/**
	 * 封面
	 */
	@Schema(description = "封面")
	private String cover;
	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Integer sort;
	/**
	 * 图片数量
	 */
	@Schema(description = "图片数量")
	private Long imgCount;
	/**
	 * 收藏数量
	 */
	@Schema(description = "收藏数量")
	private Long collectionCount;

}
