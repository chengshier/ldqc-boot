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
package org.springblade.modules.newscategory.service.impl;

import org.springblade.modules.newscategory.pojo.entity.NewsCategoryEntity;
import org.springblade.modules.newscategory.pojo.vo.NewsCategoryVO;
import org.springblade.modules.newscategory.excel.NewsCategoryExcel;
import org.springblade.modules.newscategory.mapper.NewsCategoryMapper;
import org.springblade.modules.newscategory.service.INewsCategoryService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 新闻分类表 服务实现类
 *
 * @author BladeX
 * @since 2026-03-02
 */
@Service
public class NewsCategoryServiceImpl extends BaseServiceImpl<NewsCategoryMapper, NewsCategoryEntity> implements INewsCategoryService {

	@Override
	public IPage<NewsCategoryVO> selectNewsCategoryPage(IPage<NewsCategoryVO> page, NewsCategoryVO newsCategory) {
		return page.setRecords(baseMapper.selectNewsCategoryPage(page, newsCategory));
	}


	@Override
	public List<NewsCategoryExcel> exportNewsCategory(Wrapper<NewsCategoryEntity> queryWrapper) {
		List<NewsCategoryExcel> newsCategoryList = baseMapper.exportNewsCategory(queryWrapper);
		//newsCategoryList.forEach(newsCategory -> {
		//	newsCategory.setTypeName(DictCache.getValue(DictEnum.YES_NO, NewsCategory.getType()));
		//});
		return newsCategoryList;
	}

}
