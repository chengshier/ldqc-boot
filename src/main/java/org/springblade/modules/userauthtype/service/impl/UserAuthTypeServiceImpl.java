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
package org.springblade.modules.userauthtype.service.impl;

import org.springblade.modules.userauthtype.pojo.entity.UserAuthTypeEntity;
import org.springblade.modules.userauthtype.pojo.vo.UserAuthTypeVO;
import org.springblade.modules.userauthtype.excel.UserAuthTypeExcel;
import org.springblade.modules.userauthtype.mapper.UserAuthTypeMapper;
import org.springblade.modules.userauthtype.service.IUserAuthTypeService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 用户认证类型表 服务实现类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Service
public class UserAuthTypeServiceImpl extends BaseServiceImpl<UserAuthTypeMapper, UserAuthTypeEntity> implements IUserAuthTypeService {

	@Override
	public IPage<UserAuthTypeVO> selectUserAuthTypePage(IPage<UserAuthTypeVO> page, UserAuthTypeVO userAuthType) {
		return page.setRecords(baseMapper.selectUserAuthTypePage(page, userAuthType));
	}


	@Override
	public List<UserAuthTypeExcel> exportUserAuthType(Wrapper<UserAuthTypeEntity> queryWrapper) {
		List<UserAuthTypeExcel> userAuthTypeList = baseMapper.exportUserAuthType(queryWrapper);
		//userAuthTypeList.forEach(userAuthType -> {
		//	userAuthType.setTypeName(DictCache.getValue(DictEnum.YES_NO, UserAuthType.getType()));
		//});
		return userAuthTypeList;
	}

}
