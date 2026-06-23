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
package org.springblade.modules.sportinviteapply.service.impl;

import org.springblade.modules.sportinviteapply.pojo.entity.SportInviteApplyEntity;
import org.springblade.modules.sportinviteapply.pojo.vo.SportInviteApplyVO;
import org.springblade.modules.sportinviteapply.excel.SportInviteApplyExcel;
import org.springblade.modules.sportinviteapply.mapper.SportInviteApplyMapper;
import org.springblade.modules.sportinviteapply.service.ISportInviteApplyService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 运动邀约申请表 服务实现类
 *
 * @author BladeX
 * @since 2026-05-21
 */
@Service
public class SportInviteApplyServiceImpl extends BaseServiceImpl<SportInviteApplyMapper, SportInviteApplyEntity> implements ISportInviteApplyService {

	@Override
	public IPage<SportInviteApplyVO> selectSportInviteApplyPage(IPage<SportInviteApplyVO> page, SportInviteApplyVO sportInviteApply) {
		return page.setRecords(baseMapper.selectSportInviteApplyPage(page, sportInviteApply));
	}


	@Override
	public List<SportInviteApplyExcel> exportSportInviteApply(Wrapper<SportInviteApplyEntity> queryWrapper) {
		List<SportInviteApplyExcel> sportInviteApplyList = baseMapper.exportSportInviteApply(queryWrapper);
		//sportInviteApplyList.forEach(sportInviteApply -> {
		//	sportInviteApply.setTypeName(DictCache.getValue(DictEnum.YES_NO, SportInviteApply.getType()));
		//});
		return sportInviteApplyList;
	}

}
