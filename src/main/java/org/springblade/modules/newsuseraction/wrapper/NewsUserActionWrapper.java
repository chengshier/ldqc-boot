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
package org.springblade.modules.newsuseraction.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.newsuseraction.pojo.entity.NewsUserActionEntity;
import org.springblade.modules.newsuseraction.pojo.vo.NewsUserActionVO;
import java.util.Objects;

/**
 * 用户行为表 包装类,返回视图层所需的字段
 *
 * @author BladeX
 * @since 2026-03-02
 */
public class NewsUserActionWrapper extends BaseEntityWrapper<NewsUserActionEntity, NewsUserActionVO>  {

	public static NewsUserActionWrapper build() {
		return new NewsUserActionWrapper();
 	}

	@Override
	public NewsUserActionVO entityVO(NewsUserActionEntity newsUserAction) {
		NewsUserActionVO newsUserActionVO = Objects.requireNonNull(BeanUtil.copyProperties(newsUserAction, NewsUserActionVO.class));

		//User createUser = UserCache.getUser(newsUserAction.getCreateUser());
		//User updateUser = UserCache.getUser(newsUserAction.getUpdateUser());
		//newsUserActionVO.setCreateUserName(createUser.getName());
		//newsUserActionVO.setUpdateUserName(updateUser.getName());

		return newsUserActionVO;
	}


}
