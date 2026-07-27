package org.springblade.modules.userbehaviorevent.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.userbehaviorevent.excel.UserBehaviorEventExcel;
import org.springblade.modules.userbehaviorevent.mapper.UserBehaviorEventMapper;
import org.springblade.modules.userbehaviorevent.pojo.entity.UserBehaviorEventEntity;
import org.springblade.modules.userbehaviorevent.pojo.vo.UserBehaviorEventVO;
import org.springblade.modules.userbehaviorevent.service.IUserBehaviorEventService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBehaviorEventServiceImpl extends BaseServiceImpl<UserBehaviorEventMapper, UserBehaviorEventEntity> implements IUserBehaviorEventService {

	@Override
	public IPage<UserBehaviorEventVO> selectUserBehaviorEventPage(IPage<UserBehaviorEventVO> page, UserBehaviorEventVO userBehaviorEvent) {
		return page.setRecords(baseMapper.selectUserBehaviorEventPage(page, userBehaviorEvent));
	}

	@Override
	public List<UserBehaviorEventExcel> exportUserBehaviorEvent(Wrapper<UserBehaviorEventEntity> queryWrapper) {
		return baseMapper.exportUserBehaviorEvent(queryWrapper);
	}

	@Override
	public boolean existsByRequestId(String requestId) {
		if (requestId == null || requestId.trim().isEmpty()) {
			return false;
		}
		return this.count(Wrappers.<UserBehaviorEventEntity>lambdaQuery()
			.eq(UserBehaviorEventEntity::getRequestId, requestId)
			.eq(UserBehaviorEventEntity::getIsDeleted, 0)) > 0;
	}
}
