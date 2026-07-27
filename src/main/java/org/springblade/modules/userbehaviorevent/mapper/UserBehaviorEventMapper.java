package org.springblade.modules.userbehaviorevent.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.springblade.modules.userbehaviorevent.excel.UserBehaviorEventExcel;
import org.springblade.modules.userbehaviorevent.pojo.entity.UserBehaviorEventEntity;
import org.springblade.modules.userbehaviorevent.pojo.vo.UserBehaviorEventVO;

import java.util.List;

public interface UserBehaviorEventMapper extends BaseMapper<UserBehaviorEventEntity> {

	List<UserBehaviorEventVO> selectUserBehaviorEventPage(IPage page, UserBehaviorEventVO userBehaviorEvent);

	List<UserBehaviorEventExcel> exportUserBehaviorEvent(@Param("ew") Wrapper<UserBehaviorEventEntity> queryWrapper);
}
