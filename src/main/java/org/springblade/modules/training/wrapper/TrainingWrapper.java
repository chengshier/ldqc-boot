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
package org.springblade.modules.training.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.training.pojo.vo.TrainingVO;
import java.util.Objects;

/**
 * 培训课程表 包装类,返回视图层所需的字段
 *
 * @author BladeX
 * @since 2026-03-10
 */
public class TrainingWrapper extends BaseEntityWrapper<TrainingEntity, TrainingVO>  {

	public static TrainingWrapper build() {
		return new TrainingWrapper();
 	}

	@Override
	public TrainingVO entityVO(TrainingEntity training) {
		TrainingVO trainingVO = Objects.requireNonNull(BeanUtil.copyProperties(training, TrainingVO.class));

		//User createUser = UserCache.getUser(training.getCreateUser());
		//User updateUser = UserCache.getUser(training.getUpdateUser());
		//trainingVO.setCreateUserName(createUser.getName());
		//trainingVO.setUpdateUserName(updateUser.getName());

		return trainingVO;
	}


}
