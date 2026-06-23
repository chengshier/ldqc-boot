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
package org.springblade.modules.trainingorg.service.impl;

import org.springblade.modules.trainingorg.pojo.entity.TrainingOrgEntity;
import org.springblade.modules.trainingorg.pojo.vo.TrainingOrgVO;
import org.springblade.modules.trainingorg.excel.TrainingOrgExcel;
import org.springblade.modules.trainingorg.mapper.TrainingOrgMapper;
import org.springblade.modules.trainingorg.service.ITrainingOrgService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 培训机构表 服务实现类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Service
public class TrainingOrgServiceImpl extends BaseServiceImpl<TrainingOrgMapper, TrainingOrgEntity> implements ITrainingOrgService {

	@Override
	public IPage<TrainingOrgVO> selectTrainingOrgPage(IPage<TrainingOrgVO> page, TrainingOrgVO trainingOrg) {
		return page.setRecords(baseMapper.selectTrainingOrgPage(page, trainingOrg));
	}


	@Override
	public List<TrainingOrgExcel> exportTrainingOrg(Wrapper<TrainingOrgEntity> queryWrapper) {
		List<TrainingOrgExcel> trainingOrgList = baseMapper.exportTrainingOrg(queryWrapper);
		//trainingOrgList.forEach(trainingOrg -> {
		//	trainingOrg.setTypeName(DictCache.getValue(DictEnum.YES_NO, TrainingOrg.getType()));
		//});
		return trainingOrgList;
	}

}
