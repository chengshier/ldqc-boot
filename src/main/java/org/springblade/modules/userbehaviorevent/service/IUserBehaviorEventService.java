package org.springblade.modules.userbehaviorevent.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.userbehaviorevent.excel.UserBehaviorEventExcel;
import org.springblade.modules.userbehaviorevent.pojo.entity.UserBehaviorEventEntity;
import org.springblade.modules.userbehaviorevent.pojo.vo.UserBehaviorEventVO;

import java.util.List;

public interface IUserBehaviorEventService extends BaseService<UserBehaviorEventEntity> {

	IPage<UserBehaviorEventVO> selectUserBehaviorEventPage(IPage<UserBehaviorEventVO> page, UserBehaviorEventVO userBehaviorEvent);

	List<UserBehaviorEventExcel> exportUserBehaviorEvent(Wrapper<UserBehaviorEventEntity> queryWrapper);

	boolean existsByRequestId(String requestId);
}
