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
package org.springblade.modules.venue.service.impl;

import org.springblade.modules.venue.pojo.entity.VenueEntity;
import org.springblade.modules.venue.pojo.vo.VenueVO;
import org.springblade.modules.venue.excel.VenueExcel;
import org.springblade.modules.venue.mapper.VenueMapper;
import org.springblade.modules.venue.service.IVenueService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 体育场馆表 服务实现类
 *
 * @author BladeX
 * @since 2026-03-10
 */
@Service
public class VenueServiceImpl extends BaseServiceImpl<VenueMapper, VenueEntity> implements IVenueService {

	@Override
	public IPage<VenueVO> selectVenuePage(IPage<VenueVO> page, VenueVO venue) {
		return page.setRecords(baseMapper.selectVenuePage(page, venue));
	}


	@Override
	public List<VenueExcel> exportVenue(Wrapper<VenueEntity> queryWrapper) {
		List<VenueExcel> venueList = baseMapper.exportVenue(queryWrapper);
		//venueList.forEach(venue -> {
		//	venue.setTypeName(DictCache.getValue(DictEnum.YES_NO, Venue.getType()));
		//});
		return venueList;
	}

}
