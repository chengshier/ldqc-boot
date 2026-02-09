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
package org.springblade.modules.messageuserrelation.service.impl;

import org.springblade.modules.messageuserrelation.pojo.entity.MessageUserRelationEntity;
import org.springblade.modules.messageuserrelation.pojo.vo.MessageUserRelationVO;
import org.springblade.modules.messageuserrelation.excel.MessageUserRelationExcel;
import org.springblade.modules.messageuserrelation.mapper.MessageUserRelationMapper;
import org.springblade.modules.messageuserrelation.service.IMessageUserRelationService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 消息用户关系表 服务实现类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Service
public class MessageUserRelationServiceImpl extends BaseServiceImpl<MessageUserRelationMapper, MessageUserRelationEntity> implements IMessageUserRelationService {

	@Override
	public IPage<MessageUserRelationVO> selectMessageUserRelationPage(IPage<MessageUserRelationVO> page, MessageUserRelationVO messageUserRelation) {
		return page.setRecords(baseMapper.selectMessageUserRelationPage(page, messageUserRelation));
	}


	@Override
	public List<MessageUserRelationExcel> exportMessageUserRelation(Wrapper<MessageUserRelationEntity> queryWrapper) {
		List<MessageUserRelationExcel> messageUserRelationList = baseMapper.exportMessageUserRelation(queryWrapper);
		//messageUserRelationList.forEach(messageUserRelation -> {
		//	messageUserRelation.setTypeName(DictCache.getValue(DictEnum.YES_NO, MessageUserRelation.getType()));
		//});
		return messageUserRelationList;
	}

}
