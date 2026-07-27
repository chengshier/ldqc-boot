package org.springblade.modules.userbehaviorevent.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.userbehaviorevent.pojo.entity.UserBehaviorEventEntity;
import org.springblade.modules.userbehaviorevent.pojo.vo.UserBehaviorEventVO;

import java.util.Objects;

public class UserBehaviorEventWrapper extends BaseEntityWrapper<UserBehaviorEventEntity, UserBehaviorEventVO> {

	public static UserBehaviorEventWrapper build() {
		return new UserBehaviorEventWrapper();
	}

	@Override
	public UserBehaviorEventVO entityVO(UserBehaviorEventEntity userBehaviorEvent) {
		return Objects.requireNonNull(BeanUtil.copyProperties(userBehaviorEvent, UserBehaviorEventVO.class));
	}
}
