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
package org.springblade.modules.pointssigninstat.service.impl;

import org.springblade.modules.pointssigninstat.pojo.entity.PointsSigninStatEntity;
import org.springblade.modules.pointssigninstat.pojo.vo.PointsSigninStatVO;
import org.springblade.modules.pointssigninstat.excel.PointsSigninStatExcel;
import org.springblade.modules.pointssigninstat.mapper.PointsSigninStatMapper;
import org.springblade.modules.pointssigninstat.service.IPointsSigninStatService;
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
public class PointsSigninStatServiceImpl extends BaseServiceImpl<PointsSigninStatMapper, PointsSigninStatEntity> implements IPointsSigninStatService {

	@Override
	public IPage<PointsSigninStatVO> selectPointsSigninStatPage(IPage<PointsSigninStatVO> page, PointsSigninStatVO pointsSigninStat) {
		return page.setRecords(baseMapper.selectPointsSigninStatPage(page, pointsSigninStat));
	}


	@Override
	public List<PointsSigninStatExcel> exportPointsSigninStat(Wrapper<PointsSigninStatEntity> queryWrapper) {
		List<PointsSigninStatExcel> pointsSigninStatList = baseMapper.exportPointsSigninStat(queryWrapper);
		//pointsSigninStatList.forEach(pointsSigninStat -> {
		//	pointsSigninStat.setTypeName(DictCache.getValue(DictEnum.YES_NO, PointsSigninStat.getType()));
		//});
		return pointsSigninStatList;
	}

}

