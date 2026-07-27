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
package org.springblade.modules.bannerposition.service.impl;

import org.springblade.modules.bannerposition.pojo.entity.BannerPositionEntity;
import org.springblade.modules.bannerposition.pojo.vo.BannerPositionVO;
import org.springblade.modules.bannerposition.excel.BannerPositionExcel;
import org.springblade.modules.bannerposition.mapper.BannerPositionMapper;
import org.springblade.modules.bannerposition.service.IBannerPositionService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 宣传Banner位置表 服务实现类
 *
 * @author BladeX
 * @since 2026-07-06
 */
@Service
public class BannerPositionServiceImpl extends BaseServiceImpl<BannerPositionMapper, BannerPositionEntity> implements IBannerPositionService {

	@Override
	public IPage<BannerPositionVO> selectBannerPositionPage(IPage<BannerPositionVO> page, BannerPositionVO bannerPosition) {
		return page.setRecords(baseMapper.selectBannerPositionPage(page, bannerPosition));
	}


	@Override
	public List<BannerPositionExcel> exportBannerPosition(Wrapper<BannerPositionEntity> queryWrapper) {
		List<BannerPositionExcel> bannerPositionList = baseMapper.exportBannerPosition(queryWrapper);
		//bannerPositionList.forEach(bannerPosition -> {
		//	bannerPosition.setTypeName(DictCache.getValue(DictEnum.YES_NO, BannerPosition.getType()));
		//});
		return bannerPositionList;
	}

}
