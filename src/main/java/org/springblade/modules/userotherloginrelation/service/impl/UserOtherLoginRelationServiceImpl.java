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
package org.springblade.modules.userotherloginrelation.service.impl;

import org.springblade.modules.userotherloginrelation.pojo.entity.UserOtherLoginRelationEntity;
import org.springblade.modules.userotherloginrelation.pojo.vo.UserOtherLoginRelationVO;
import org.springblade.modules.userotherloginrelation.excel.UserOtherLoginRelationExcel;
import org.springblade.modules.userotherloginrelation.mapper.UserOtherLoginRelationMapper;
import org.springblade.modules.userotherloginrelation.service.IUserOtherLoginRelationService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 用户第三方登录关系表 服务实现类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Service
public class UserOtherLoginRelationServiceImpl extends BaseServiceImpl<UserOtherLoginRelationMapper, UserOtherLoginRelationEntity> implements IUserOtherLoginRelationService {

	@Override
	public IPage<UserOtherLoginRelationVO> selectUserOtherLoginRelationPage(IPage<UserOtherLoginRelationVO> page, UserOtherLoginRelationVO userOtherLoginRelation) {
		return page.setRecords(baseMapper.selectUserOtherLoginRelationPage(page, userOtherLoginRelation));
	}


	@Override
	public List<UserOtherLoginRelationExcel> exportUserOtherLoginRelation(Wrapper<UserOtherLoginRelationEntity> queryWrapper) {
		List<UserOtherLoginRelationExcel> userOtherLoginRelationList = baseMapper.exportUserOtherLoginRelation(queryWrapper);
		//userOtherLoginRelationList.forEach(userOtherLoginRelation -> {
		//	userOtherLoginRelation.setTypeName(DictCache.getValue(DictEnum.YES_NO, UserOtherLoginRelation.getType()));
		//});
		return userOtherLoginRelationList;
	}

}
