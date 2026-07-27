package org.springblade.modules.userinterest.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.userinterest.pojo.entity.UserInterestEntity;

import java.util.List;

public interface IUserInterestService extends BaseService<UserInterestEntity> {
	List<UserInterestEntity> listByUserId(Long userId);
	void replaceForUser(Long userId, List<Long> categoryIds);
}
