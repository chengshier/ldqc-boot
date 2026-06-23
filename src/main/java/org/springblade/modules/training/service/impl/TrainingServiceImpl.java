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
package org.springblade.modules.training.service.impl;

import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.training.pojo.vo.TrainingVO;
import org.springblade.modules.training.excel.TrainingExcel;
import org.springblade.modules.training.mapper.TrainingMapper;
import org.springblade.modules.training.service.ITrainingService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 培训课程表 服务实现类
 *
 * @author BladeX
 * @since 2026-03-10
 */
@Service
public class TrainingServiceImpl extends BaseServiceImpl<TrainingMapper, TrainingEntity> implements ITrainingService {

	@Override
	public IPage<TrainingVO> selectTrainingPage(IPage<TrainingVO> page, TrainingVO training) {
		return page.setRecords(baseMapper.selectTrainingPage(page, training));
	}


	@Override
	public List<TrainingExcel> exportTraining(Wrapper<TrainingEntity> queryWrapper) {
		List<TrainingExcel> trainingList = baseMapper.exportTraining(queryWrapper);
		//trainingList.forEach(training -> {
		//	training.setTypeName(DictCache.getValue(DictEnum.YES_NO, Training.getType()));
		//});
		return trainingList;
	}

}
