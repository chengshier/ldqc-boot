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
package org.springblade.modules.news.service.impl;

import org.springblade.modules.news.pojo.entity.NewsEntity;
import org.springblade.modules.news.pojo.vo.NewsVO;
import org.springblade.modules.news.excel.NewsExcel;
import org.springblade.modules.news.mapper.NewsMapper;
import org.springblade.modules.news.service.INewsService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 新闻表 服务实现类
 *
 * @author BladeX
 * @since 2026-03-02
 */
@Service
public class NewsServiceImpl extends BaseServiceImpl<NewsMapper, NewsEntity> implements INewsService {

	@Override
	public IPage<NewsVO> selectNewsPage(IPage<NewsVO> page, NewsVO news) {
		// 默认只查询已发布的
		if (news != null && news.getNewsStatus() == null) {
			news.setNewsStatus(1);
		}
		List<NewsVO> records = baseMapper.selectNewsPage(page, news);
		return page.setRecords(records);
	}


	@Override
	public NewsEntity getTopNews() {
		return baseMapper.getTopNews();
	}

	@Override
	public NewsEntity getNewsDetail(Long id) {
		// 1. 查询新闻
		NewsEntity news = this.getById(id);
		if (news != null) {
			// 2. 增加浏览量
			baseMapper.incrementViewCount(id);
			// 重新查询获取最新数据
			news = this.getById(id);
		}
		return news;
	}


	@Override
	public List<NewsExcel> exportNews(Wrapper<NewsEntity> queryWrapper) {
		List<NewsExcel> newsList = baseMapper.exportNews(queryWrapper);
		//newsList.forEach(news -> {
		//	news.setTypeName(DictCache.getValue(DictEnum.YES_NO, News.getType()));
		//});
		return newsList;
	}

}
