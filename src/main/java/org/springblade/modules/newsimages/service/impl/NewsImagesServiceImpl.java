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
package org.springblade.modules.newsimages.service.impl;

import org.springblade.modules.newsimages.pojo.entity.NewsImagesEntity;
import org.springblade.modules.newsimages.pojo.vo.NewsImagesVO;
import org.springblade.modules.newsimages.excel.NewsImagesExcel;
import org.springblade.modules.newsimages.mapper.NewsImagesMapper;
import org.springblade.modules.newsimages.service.INewsImagesService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 新闻图片表 服务实现类
 *
 * @author BladeX
 * @since 2026-03-02
 */
@Service
public class NewsImagesServiceImpl extends BaseServiceImpl<NewsImagesMapper, NewsImagesEntity> implements INewsImagesService {

	@Override
	public IPage<NewsImagesVO> selectNewsImagesPage(IPage<NewsImagesVO> page, NewsImagesVO newsImages) {
		return page.setRecords(baseMapper.selectNewsImagesPage(page, newsImages));
	}


	@Override
	public List<NewsImagesExcel> exportNewsImages(Wrapper<NewsImagesEntity> queryWrapper) {
		List<NewsImagesExcel> newsImagesList = baseMapper.exportNewsImages(queryWrapper);
		//newsImagesList.forEach(newsImages -> {
		//	newsImages.setTypeName(DictCache.getValue(DictEnum.YES_NO, NewsImages.getType()));
		//});
		return newsImagesList;
	}

}
