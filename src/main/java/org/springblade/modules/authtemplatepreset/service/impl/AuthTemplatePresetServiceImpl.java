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
package org.springblade.modules.authtemplatepreset.service.impl;

import org.springblade.modules.authtemplatepreset.pojo.entity.AuthTemplatePresetEntity;
import org.springblade.modules.authtemplatepreset.pojo.vo.AuthTemplatePresetVO;
import org.springblade.modules.authtemplatepreset.excel.AuthTemplatePresetExcel;
import org.springblade.modules.authtemplatepreset.mapper.AuthTemplatePresetMapper;
import org.springblade.modules.authtemplatepreset.service.IAuthTemplatePresetService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 认证模板推荐项(字段/附件) 服务实现类
 *
 * @author BladeX
 * @since 2026-04-09
 */
@Service
public class AuthTemplatePresetServiceImpl extends BaseServiceImpl<AuthTemplatePresetMapper, AuthTemplatePresetEntity> implements IAuthTemplatePresetService {

	@Override
	public IPage<AuthTemplatePresetVO> selectAuthTemplatePresetPage(IPage<AuthTemplatePresetVO> page, AuthTemplatePresetVO authTemplatePreset) {
		return page.setRecords(baseMapper.selectAuthTemplatePresetPage(page, authTemplatePreset));
	}


	@Override
	public List<AuthTemplatePresetExcel> exportAuthTemplatePreset(Wrapper<AuthTemplatePresetEntity> queryWrapper) {
		List<AuthTemplatePresetExcel> authTemplatePresetList = baseMapper.exportAuthTemplatePreset(queryWrapper);
		//authTemplatePresetList.forEach(authTemplatePreset -> {
		//	authTemplatePreset.setTypeName(DictCache.getValue(DictEnum.YES_NO, AuthTemplatePreset.getType()));
		//});
		return authTemplatePresetList;
	}

}
