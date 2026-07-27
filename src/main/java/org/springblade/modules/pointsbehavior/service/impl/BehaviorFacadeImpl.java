package org.springblade.modules.pointsbehavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.pointsbehavior.pojo.dto.BehaviorAwardResult;
import org.springblade.modules.pointsbehavior.pojo.dto.BehaviorCommand;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorBizType;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorEventCode;
import org.springblade.modules.pointsbehavior.service.IBehaviorFacade;
import org.springblade.modules.pointsrule.pojo.entity.PointsRuleEntity;
import org.springblade.modules.pointsrule.service.IPointsRuleService;
import org.springblade.modules.pointsrulecondition.pojo.entity.PointsRuleConditionEntity;
import org.springblade.modules.pointsrulecondition.service.IPointsRuleConditionService;
import org.springblade.modules.userbehaviorevent.pojo.entity.UserBehaviorEventEntity;
import org.springblade.modules.userbehaviorevent.service.IUserBehaviorEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BehaviorFacadeImpl implements IBehaviorFacade {

	private static final String DEFAULT_SOURCE = "SYSTEM";

	private final IUserBehaviorEventService userBehaviorEventService;
	private final IPointsRuleService pointsRuleService;
	private final IPointsRuleConditionService pointsRuleConditionService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void onSuccess(BehaviorCommand command) {
		onSuccessWithResult(command);
	}

	@Override
	public void onSuccess(BehaviorEventCode eventCode, BehaviorBizType bizType, String bizId, Long userId, String requestId, Map<String, Object> ext) {
		onSuccessWithResult(eventCode, bizType, bizId, userId, requestId, ext);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public BehaviorAwardResult onSuccessWithResult(BehaviorCommand command) {
		if (command == null || Func.isEmpty(command.getUserId()) || Func.isBlank(command.getEventCode()) || Func.isBlank(command.getBizType()) || Func.isBlank(command.getBizId())) {
			return BehaviorAwardResult.empty(command == null ? null : command.getEventCode(), command == null ? null : command.getRequestId(), "行为参数不完整");
		}
		String rawRequestId = Func.isBlank(command.getRequestId()) ? UUID.randomUUID().toString() : command.getRequestId();
		String requestId = compactRequestId(rawRequestId);
		if (userBehaviorEventService.existsByRequestId(requestId)) {
			return BehaviorAwardResult.empty(command.getEventCode(), requestId, "行为事件已处理");
		}

		Map<String, Object> payload = new HashMap<>();
		if (command.getExt() != null) {
			payload.putAll(command.getExt());
		}
		payload.put("eventCode", command.getEventCode());
		payload.put("bizType", command.getBizType());
		payload.put("bizId", command.getBizId());
		payload.put("userId", command.getUserId());

		UserBehaviorEventEntity eventEntity = new UserBehaviorEventEntity();
		eventEntity.setEventCode(command.getEventCode());
		eventEntity.setUserId(command.getUserId());
		eventEntity.setBizType(command.getBizType());
		eventEntity.setBizId(command.getBizId());
		eventEntity.setEventStatus(1);
		eventEntity.setRequestId(requestId);
		eventEntity.setSource(Func.isBlank(command.getSource()) ? DEFAULT_SOURCE : command.getSource());
		eventEntity.setEventTime(new Date());
		eventEntity.setExtJson(command.getExt() == null || command.getExt().isEmpty() ? null : JSON.toJSONString(command.getExt()));
		userBehaviorEventService.save(eventEntity);

		List<PointsRuleEntity> rules = pointsRuleService.list(Wrappers.<PointsRuleEntity>lambdaQuery()
			.eq(PointsRuleEntity::getSceneType, command.getEventCode())
			.eq(PointsRuleEntity::getStatus, 1)
			.eq(PointsRuleEntity::getIsDeleted, 0)
			.orderByAsc(PointsRuleEntity::getId));
		if (rules == null || rules.isEmpty()) {
			return BehaviorAwardResult.empty(command.getEventCode(), requestId, "未配置积分规则");
		}

		int matchedRuleCount = 0;
		int grantedRuleCount = 0;
		int grantedPoints = 0;
		String lastMessage = "未命中奖励规则";
		for (PointsRuleEntity rule : rules) {
			if (!matchRule(rule, payload)) {
				continue;
			}
			matchedRuleCount++;
			String ruleRequestId = compactRequestId(requestId + ":" + rule.getRuleCode());
			String grantMessage = pointsRuleService.grantPointsByRule(
				command.getUserId(),
				rule.getRuleCode(),
				command.getBizType(),
				command.getBizId(),
				ruleRequestId,
				rule.getRuleName()
			);
			lastMessage = grantMessage;
			if (grantMessage != null && grantMessage.startsWith("发放成功")) {
				grantedRuleCount++;
				grantedPoints += extractGrantPoints(grantMessage);
			}
		}
		return BehaviorAwardResult.builder()
			.eventCode(command.getEventCode())
			.requestId(requestId)
			.matchedRuleCount(matchedRuleCount)
			.grantedRuleCount(grantedRuleCount)
			.grantedPoints(grantedPoints)
			.message(lastMessage)
			.build();
	}

	@Override
	public BehaviorAwardResult onSuccessWithResult(BehaviorEventCode eventCode, BehaviorBizType bizType, String bizId, Long userId, String requestId, Map<String, Object> ext) {
		if (eventCode == null || bizType == null) {
			return BehaviorAwardResult.empty(null, requestId, "行为类型不能为空");
		}
		return onSuccessWithResult(BehaviorCommand.builder()
			.eventCode(eventCode.getCode())
			.bizType(bizType.getCode())
			.bizId(bizId)
			.userId(userId)
			.requestId(requestId)
			.ext(ext == null ? new HashMap<>() : ext)
			.build());
	}

	private boolean matchRule(PointsRuleEntity rule, Map<String, Object> payload) {
		List<PointsRuleConditionEntity> conditions = pointsRuleConditionService.listByRuleCode(rule.getRuleCode());
		if (conditions == null || conditions.isEmpty()) {
			return matchExtJson(rule.getExtJson(), payload);
		}
		Map<Integer, List<PointsRuleConditionEntity>> grouped = conditions.stream()
			.collect(Collectors.groupingBy(item -> item.getConditionGroup() == null ? 1 : item.getConditionGroup(), LinkedHashMap::new, Collectors.toList()));
		for (List<PointsRuleConditionEntity> group : grouped.values()) {
			boolean groupMatched = true;
			for (PointsRuleConditionEntity condition : group) {
				if (!matchCondition(condition, payload)) {
					groupMatched = false;
					break;
				}
			}
			if (groupMatched) {
				return true;
			}
		}
		return false;
	}

	private boolean matchExtJson(String extJson, Map<String, Object> payload) {
		if (Func.isBlank(extJson)) {
			return true;
		}
		try {
			JSONObject jsonObject = JSON.parseObject(extJson);
			for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
				Object payloadValue = payload.get(entry.getKey());
				if (!Objects.equals(normalize(payloadValue), normalize(entry.getValue()))) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			return true;
		}
	}

	private boolean matchCondition(PointsRuleConditionEntity condition, Map<String, Object> payload) {
		Object payloadValue = payload.get(condition.getConditionKey());
		String op = Func.toStr(condition.getConditionOp()).toLowerCase(Locale.ROOT);
		String conditionValue = condition.getConditionValue();
		switch (op) {
			case "eq":
				return Objects.equals(normalize(payloadValue), normalize(conditionValue));
			case "ne":
				return !Objects.equals(normalize(payloadValue), normalize(conditionValue));
			case "in":
				return parseList(conditionValue).contains(normalize(payloadValue));
			case "not_in":
				return !parseList(conditionValue).contains(normalize(payloadValue));
			case "contains":
				return payloadValue != null && String.valueOf(payloadValue).contains(Func.toStr(conditionValue));
			case "gt":
				return compareNumber(payloadValue, conditionValue) > 0;
			case "gte":
				return compareNumber(payloadValue, conditionValue) >= 0;
			case "lt":
				return compareNumber(payloadValue, conditionValue) < 0;
			case "lte":
				return compareNumber(payloadValue, conditionValue) <= 0;
			default:
				return true;
		}
	}

	private String normalize(Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	private List<String> parseList(String raw) {
		if (Func.isBlank(raw)) {
			return Collections.emptyList();
		}
		try {
			JSONArray array = JSON.parseArray(raw);
			List<String> values = new ArrayList<>();
			for (Object item : array) {
				values.add(normalize(item));
			}
			return values;
		} catch (Exception e) {
			return Arrays.stream(raw.split(","))
				.map(String::trim)
				.filter(item -> !item.isEmpty())
				.collect(Collectors.toList());
		}
	}

	private int extractGrantPoints(String grantMessage) {
		if (Func.isBlank(grantMessage)) {
			return 0;
		}
		try {
			int plusIndex = grantMessage.indexOf('+');
			int beanIndex = grantMessage.indexOf("绿豆");
			if (plusIndex >= 0 && beanIndex > plusIndex) {
				return Integer.parseInt(grantMessage.substring(plusIndex + 1, beanIndex));
			}
		} catch (Exception ignored) {
		}
		return 0;
	}

	private String compactRequestId(String requestId) {
		String value = Func.toStr(requestId).trim();
		if (value.length() <= 32) {
			return value;
		}
		return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8))
			.toString()
			.replace("-", "");
	}

	private int compareNumber(Object payloadValue, String conditionValue) {
		try {
			BigDecimal left = new BigDecimal(String.valueOf(payloadValue));
			BigDecimal right = new BigDecimal(String.valueOf(conditionValue));
			return left.compareTo(right);
		} catch (Exception e) {
			return -1;
		}
	}
}
