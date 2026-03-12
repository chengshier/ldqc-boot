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
package org.springblade.modules.newsuseraction.service.impl;

import org.springblade.modules.newsuseraction.pojo.entity.NewsUserActionEntity;
import org.springblade.modules.newsuseraction.pojo.vo.NewsUserActionVO;
import org.springblade.modules.newsuseraction.excel.NewsUserActionExcel;
import org.springblade.modules.newsuseraction.mapper.NewsUserActionMapper;
import org.springblade.modules.newsuseraction.service.INewsUserActionService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 用户行为表 服务实现类
 *
 * @author BladeX
 * @since 2026-03-02
 */
@Service
public class NewsUserActionServiceImpl extends BaseServiceImpl<NewsUserActionMapper, NewsUserActionEntity> implements INewsUserActionService {

	@Override
	public IPage<NewsUserActionVO> selectNewsUserActionPage(IPage<NewsUserActionVO> page, NewsUserActionVO newsUserAction) {
		return page.setRecords(baseMapper.selectNewsUserActionPage(page, newsUserAction));
	}


	@Override
	public List<NewsUserActionExcel> exportNewsUserAction(Wrapper<NewsUserActionEntity> queryWrapper) {
		List<NewsUserActionExcel> newsUserActionList = baseMapper.exportNewsUserAction(queryWrapper);
		//newsUserActionList.forEach(newsUserAction -> {
		//	newsUserAction.setTypeName(DictCache.getValue(DictEnum.YES_NO, NewsUserAction.getType()));
		//});
		return newsUserActionList;
	}

}
