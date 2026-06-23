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
package org.springblade.modules.couponreceivelog.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.couponreceivelog.pojo.entity.CouponReceiveLogEntity;
import org.springblade.modules.couponreceivelog.pojo.vo.CouponReceiveLogVO;
import java.util.Objects;

/**
 * 用户认证类型表 包装类,返回视图层所需的字段
 *
 * @author BladeX
 * @since 2026-04-02
 */
public class CouponReceiveLogWrapper extends BaseEntityWrapper<CouponReceiveLogEntity, CouponReceiveLogVO>  {

	public static CouponReceiveLogWrapper build() {
		return new CouponReceiveLogWrapper();
 	}

	@Override
	public CouponReceiveLogVO entityVO(CouponReceiveLogEntity couponReceiveLog) {
		CouponReceiveLogVO couponReceiveLogVO = Objects.requireNonNull(BeanUtil.copyProperties(couponReceiveLog, CouponReceiveLogVO.class));

		//User createUser = UserCache.getUser(couponReceiveLog.getCreateUser());
		//User updateUser = UserCache.getUser(couponReceiveLog.getUpdateUser());
		//couponReceiveLogVO.setCreateUserName(createUser.getName());
		//couponReceiveLogVO.setUpdateUserName(updateUser.getName());

		return couponReceiveLogVO;
	}


}

