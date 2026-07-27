package org.springblade.modules.userinterest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.userinterest.mapper.UserInterestMapper;
import org.springblade.modules.userinterest.pojo.entity.UserInterestEntity;
import org.springblade.modules.userinterest.service.IUserInterestService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserInterestServiceImpl extends BaseServiceImpl<UserInterestMapper, UserInterestEntity> implements IUserInterestService {
	@Override
	public List<UserInterestEntity> listByUserId(Long userId) {
		return list(Wrappers.<UserInterestEntity>lambdaQuery()
			.eq(UserInterestEntity::getUserId, userId)
			.eq(UserInterestEntity::getIsDeleted, 0)
			.orderByAsc(UserInterestEntity::getSort));
	}

	@Override
	public void replaceForUser(Long userId, List<Long> categoryIds) {
		remove(Wrappers.<UserInterestEntity>lambdaQuery().eq(UserInterestEntity::getUserId, userId));
		if (categoryIds == null || categoryIds.isEmpty()) {
			return;
		}
		List<UserInterestEntity> interests = new ArrayList<>();
		for (int index = 0; index < categoryIds.size(); index++) {
			UserInterestEntity interest = new UserInterestEntity();
			interest.setUserId(userId);
			interest.setCategoryId(categoryIds.get(index));
			interest.setSort(index + 1);
			interests.add(interest);
		}
		saveBatch(interests);
	}
}
