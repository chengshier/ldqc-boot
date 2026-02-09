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
package org.springblade.modules.userthree.service.impl;

import org.springblade.modules.userthree.pojo.entity.UserThreeEntity;
import org.springblade.modules.userthree.pojo.vo.UserThreeVO;
import org.springblade.modules.userthree.excel.UserThreeExcel;
import org.springblade.modules.userthree.mapper.UserThreeMapper;
import org.springblade.modules.userthree.service.IUserThreeService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 用户微信登录认证表 服务实现类
 *
 * @author BladeX
 * @since 2026-02-04
 */
@Service
public class UserThreeServiceImpl extends BaseServiceImpl<UserThreeMapper, UserThreeEntity> implements IUserThreeService {

	@Override
	public IPage<UserThreeVO> selectUserThreePage(IPage<UserThreeVO> page, UserThreeVO userThree) {
		return page.setRecords(baseMapper.selectUserThreePage(page, userThree));
	}


	@Override
	public List<UserThreeExcel> exportUserThree(Wrapper<UserThreeEntity> queryWrapper) {
		List<UserThreeExcel> userThreeList = baseMapper.exportUserThree(queryWrapper);
		//userThreeList.forEach(userThree -> {
		//	userThree.setTypeName(DictCache.getValue(DictEnum.YES_NO, UserThree.getType()));
		//});
		return userThreeList;
	}

}
