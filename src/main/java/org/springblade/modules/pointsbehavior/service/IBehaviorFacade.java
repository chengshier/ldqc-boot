package org.springblade.modules.pointsbehavior.service;

import org.springblade.modules.pointsbehavior.pojo.dto.BehaviorAwardResult;
import org.springblade.modules.pointsbehavior.pojo.dto.BehaviorCommand;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorBizType;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorEventCode;

import java.util.Map;

public interface IBehaviorFacade {

	void onSuccess(BehaviorCommand command);

	void onSuccess(BehaviorEventCode eventCode, BehaviorBizType bizType, String bizId, Long userId, String requestId, Map<String, Object> ext);

	BehaviorAwardResult onSuccessWithResult(BehaviorCommand command);

	BehaviorAwardResult onSuccessWithResult(BehaviorEventCode eventCode, BehaviorBizType bizType, String bizId, Long userId, String requestId, Map<String, Object> ext);
}
