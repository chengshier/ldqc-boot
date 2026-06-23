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
package org.springblade.modules.couponverifylog.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.couponverifylog.pojo.entity.CouponVerifyLogEntity;
import org.springblade.modules.couponverifylog.pojo.vo.CouponVerifyLogVO;
import java.util.Objects;

/**
 * 用户认证类型表 包装类,返回视图层所需的字段
 *
 * @author BladeX
 * @since 2026-04-02
 */
public class CouponVerifyLogWrapper extends BaseEntityWrapper<CouponVerifyLogEntity, CouponVerifyLogVO>  {

	public static CouponVerifyLogWrapper build() {
		return new CouponVerifyLogWrapper();
 	}

	@Override
	public CouponVerifyLogVO entityVO(CouponVerifyLogEntity couponVerifyLog) {
		CouponVerifyLogVO couponVerifyLogVO = Objects.requireNonNull(BeanUtil.copyProperties(couponVerifyLog, CouponVerifyLogVO.class));

		//User createUser = UserCache.getUser(couponVerifyLog.getCreateUser());
		//User updateUser = UserCache.getUser(couponVerifyLog.getUpdateUser());
		//couponVerifyLogVO.setCreateUserName(createUser.getName());
		//couponVerifyLogVO.setUpdateUserName(updateUser.getName());

		return couponVerifyLogVO;
	}


}

