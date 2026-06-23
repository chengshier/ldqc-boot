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
package org.springblade.modules.outdoor.service.impl;

import org.springblade.modules.outdoor.pojo.entity.OutdoorEntity;
import org.springblade.modules.outdoor.pojo.vo.OutdoorVO;
import org.springblade.modules.outdoor.excel.OutdoorExcel;
import org.springblade.modules.outdoor.mapper.OutdoorMapper;
import org.springblade.modules.outdoor.service.IOutdoorService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.Date;
import java.util.List;

/**
 * 户外活动表 服务实现类
 *
 * @author BladeX
 * @since 2026-03-10
 */
@Service
public class OutdoorServiceImpl extends BaseServiceImpl<OutdoorMapper, OutdoorEntity> implements IOutdoorService {

	@Override
	public IPage<OutdoorVO> selectOutdoorPage(IPage<OutdoorVO> page, OutdoorVO outdoor) {
		return page.setRecords(baseMapper.selectOutdoorPage(page, outdoor));
	}


	@Override
	public List<OutdoorExcel> exportOutdoor(Wrapper<OutdoorEntity> queryWrapper) {
		List<OutdoorExcel> outdoorList = baseMapper.exportOutdoor(queryWrapper);
		//outdoorList.forEach(outdoor -> {
		//	outdoor.setTypeName(DictCache.getValue(DictEnum.YES_NO, Outdoor.getType()));
		//});
		return outdoorList;
	}

	@Override
	public int closeExpiredOutdoor() {
		return baseMapper.update(
			new OutdoorEntity(),
			Wrappers.<OutdoorEntity>lambdaUpdate()
				.set(OutdoorEntity::getStatus, 3)
				.le(OutdoorEntity::getEndTime, new Date())
				.in(OutdoorEntity::getStatus, 1, 2)
		);
	}

}
