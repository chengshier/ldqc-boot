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
package org.springblade.modules.pointsrule.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.modules.pointsbehavior.pojo.dto.BehaviorAwardResult;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorBizType;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorEventCode;
import org.springblade.modules.pointsbehavior.service.IBehaviorFacade;
import org.springblade.modules.pointsrule.pojo.entity.PointsRuleEntity;
import org.springblade.modules.pointsrule.pojo.vo.PointsRuleVO;
import org.springblade.modules.pointsrule.pojo.vo.PointsTaskStatusVO;
import org.springblade.modules.pointsrule.excel.PointsRuleExcel;
import org.springblade.modules.pointsrule.mapper.PointsRuleMapper;
import org.springblade.modules.pointsrule.service.IPointsRuleService;
import org.springblade.modules.pointsaccount.pojo.entity.PointsAccountEntity;
import org.springblade.modules.pointsaccount.service.IPointsAccountService;
import org.springblade.modules.pointsdailycounter.pojo.entity.PointsDailyCounterEntity;
import org.springblade.modules.pointsdailycounter.service.IPointsDailyCounterService;
import org.springblade.modules.pointsledger.pojo.entity.PointsLedgerEntity;
import org.springblade.modules.pointsledger.service.IPointsLedgerService;
import org.springblade.modules.pointssigninstat.pojo.entity.PointsSigninStatEntity;
import org.springblade.modules.pointssigninstat.service.IPointsSigninStatService;
import org.springblade.modules.pointstasklog.pojo.entity.PointsTaskLogEntity;
import org.springblade.modules.pointstasklog.service.IPointsTaskLogService;
import org.springblade.core.tool.utils.Func;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户认证类型表 服务实现类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
public class PointsRuleServiceImpl extends BaseServiceImpl<PointsRuleMapper, PointsRuleEntity> implements IPointsRuleService {

	private final IPointsAccountService pointsAccountService;
	private final IPointsLedgerService pointsLedgerService;
	private final IPointsSigninStatService pointsSigninStatService;
	private final IPointsTaskLogService pointsTaskLogService;
	private final IPointsDailyCounterService pointsDailyCounterService;

	@Autowired
	@Lazy
	private IBehaviorFacade behaviorFacade;

	@Override
	public IPage<PointsRuleVO> selectPointsRulePage(IPage<PointsRuleVO> page, PointsRuleVO pointsRule) {
		return page.setRecords(baseMapper.selectPointsRulePage(page, pointsRule));
	}


	@Override
	public List<PointsRuleExcel> exportPointsRule(Wrapper<PointsRuleEntity> queryWrapper) {
		List<PointsRuleExcel> pointsRuleList = baseMapper.exportPointsRule(queryWrapper);
		//pointsRuleList.forEach(pointsRule -> {
		//	pointsRule.setTypeName(DictCache.getValue(DictEnum.YES_NO, PointsRule.getType()));
		//});
		return pointsRuleList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String signIn(Long userId) {
		if (userId == null) return "请先登录";
		Date now = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Date today = c.getTime();

		PointsSigninStatEntity stat = pointsSigninStatService.getOne(Wrappers.<PointsSigninStatEntity>lambdaQuery()
			.eq(PointsSigninStatEntity::getUserId, userId)
			.eq(PointsSigninStatEntity::getIsDeleted, 0));
		if (stat == null) {
			stat = new PointsSigninStatEntity();
			stat.setUserId(userId);
			stat.setContinueDays(0);
			stat.setMonthSigninDays(0);
			pointsSigninStatService.save(stat);
		}

		if (stat.getLastSigninDate() != null) {
			Calendar last = Calendar.getInstance();
			last.setTime(stat.getLastSigninDate());
			last.set(Calendar.HOUR_OF_DAY, 0);
			last.set(Calendar.MINUTE, 0);
			last.set(Calendar.SECOND, 0);
			last.set(Calendar.MILLISECOND, 0);
			if (last.getTime().equals(today)) return "今日已签到";
		}

		int continueDays = stat.getContinueDays() == null ? 0 : stat.getContinueDays();
		if (stat.getLastSigninDate() != null) {
			Calendar lastPlus = Calendar.getInstance();
			lastPlus.setTime(stat.getLastSigninDate());
			lastPlus.set(Calendar.HOUR_OF_DAY, 0);
			lastPlus.set(Calendar.MINUTE, 0);
			lastPlus.set(Calendar.SECOND, 0);
			lastPlus.set(Calendar.MILLISECOND, 0);
			lastPlus.add(Calendar.DAY_OF_MONTH, 1);
			if (lastPlus.getTime().equals(today)) {
				continueDays += 1;
			} else {
				continueDays = 1;
			}
		} else {
			continueDays = 1;
		}

		int monthSigninDays = (stat.getMonthSigninDays() == null ? 0 : stat.getMonthSigninDays()) + 1;
		stat.setLastSigninDate(today);
		stat.setContinueDays(continueDays);
		stat.setMonthSigninDays(monthSigninDays);
		pointsSigninStatService.updateById(stat);

		String dayKey = dayKey(today);
		java.util.Map<String, Object> ext = new java.util.HashMap<>();
		ext.put("continueDays", continueDays);
		ext.put("monthSigninDays", monthSigninDays);
		ext.put("signDate", dayKey);

		BehaviorAwardResult dailyResult = behaviorFacade.onSuccessWithResult(
			BehaviorEventCode.DAILY_SIGNIN_SUCCESS,
			BehaviorBizType.SIGNIN,
			"SIGNIN_" + dayKey,
			userId,
			"REQ_SIGNIN_" + userId + "_" + dayKey,
			ext
		);
		int reward = dailyResult == null || dailyResult.getGrantedPoints() == null ? 0 : dailyResult.getGrantedPoints();

		if (continueDays % 30 == 0) {
			BehaviorAwardResult streak30 = behaviorFacade.onSuccessWithResult(
				BehaviorEventCode.SIGNIN_STREAK_30_SUCCESS,
				BehaviorBizType.SIGNIN,
				"SIGNIN_STREAK_30_" + dayKey,
				userId,
				"REQ_SIGNIN_STREAK30_" + userId + "_" + dayKey,
				ext
			);
			reward += streak30 == null || streak30.getGrantedPoints() == null ? 0 : streak30.getGrantedPoints();
		} else if (continueDays % 7 == 0) {
			BehaviorAwardResult streak7 = behaviorFacade.onSuccessWithResult(
				BehaviorEventCode.SIGNIN_STREAK_7_SUCCESS,
				BehaviorBizType.SIGNIN,
				"SIGNIN_STREAK_7_" + dayKey,
				userId,
				"REQ_SIGNIN_STREAK7_" + userId + "_" + dayKey,
				ext
			);
			reward += streak7 == null || streak7.getGrantedPoints() == null ? 0 : streak7.getGrantedPoints();
		}

		return reward > 0 ? "签到成功+" + reward + "绿豆" : "签到成功";
	}

	@Override
	public List<PointsTaskStatusVO> getCurrentUserTaskStatus(Long userId) {
		if (userId == null) {
			return java.util.Collections.emptyList();
		}
		List<PointsRuleEntity> activeRules = this.list(Wrappers.<PointsRuleEntity>lambdaQuery()
			.eq(PointsRuleEntity::getStatus, 1)
			.eq(PointsRuleEntity::getIsDeleted, 0)
			.orderByAsc(PointsRuleEntity::getId));
		if (activeRules == null || activeRules.isEmpty()) {
			return java.util.Collections.emptyList();
		}

		Set<String> ruleCodes = activeRules.stream()
			.map(PointsRuleEntity::getRuleCode)
			.filter(Func::isNotBlank)
			.collect(Collectors.toSet());

		Date today = dayStart(new Date());
		List<PointsTaskLogEntity> todayLogs = pointsTaskLogService.list(Wrappers.<PointsTaskLogEntity>lambdaQuery()
			.eq(PointsTaskLogEntity::getUserId, userId)
			.in(!ruleCodes.isEmpty(), PointsTaskLogEntity::getRuleCode, ruleCodes)
			.ge(PointsTaskLogEntity::getCreateTime, today)
			.eq(PointsTaskLogEntity::getIsDeleted, 0));
		List<PointsTaskLogEntity> historyLogs = pointsTaskLogService.list(Wrappers.<PointsTaskLogEntity>lambdaQuery()
			.eq(PointsTaskLogEntity::getUserId, userId)
			.in(!ruleCodes.isEmpty(), PointsTaskLogEntity::getRuleCode, ruleCodes)
			.eq(PointsTaskLogEntity::getIsDeleted, 0));

		Map<String, Long> todayCountMap = todayLogs.stream().collect(Collectors.groupingBy(PointsTaskLogEntity::getRuleCode, Collectors.counting()));
		Map<String, Long> historyCountMap = historyLogs.stream().collect(Collectors.groupingBy(PointsTaskLogEntity::getRuleCode, Collectors.counting()));

		List<PointsLedgerEntity> todayLedgers = pointsLedgerService.list(Wrappers.<PointsLedgerEntity>lambdaQuery()
			.eq(PointsLedgerEntity::getUserId, userId)
			.in(!ruleCodes.isEmpty(), PointsLedgerEntity::getRuleCode, ruleCodes)
			.ge(PointsLedgerEntity::getCreateTime, today)
			.eq(PointsLedgerEntity::getIsDeleted, 0));
		Map<String, Integer> todayPointsMap = new HashMap<>();
		for (PointsLedgerEntity ledger : todayLedgers) {
			String code = ledger.getRuleCode();
			todayPointsMap.put(code, todayPointsMap.getOrDefault(code, 0) + Func.toInt(ledger.getChangePoints(), 0));
		}

		PointsSigninStatEntity signStat = pointsSigninStatService.getOne(Wrappers.<PointsSigninStatEntity>lambdaQuery()
			.eq(PointsSigninStatEntity::getUserId, userId)
			.eq(PointsSigninStatEntity::getIsDeleted, 0)
			.last("limit 1"));
		boolean signedToday = signStat != null && signStat.getLastSigninDate() != null && dayStart(signStat.getLastSigninDate()).equals(today);
		int continueDays = signStat == null ? 0 : Func.toInt(signStat.getContinueDays(), 0);
		int monthSigninDays = signStat == null ? 0 : Func.toInt(signStat.getMonthSigninDays(), 0);

		List<PointsTaskStatusVO> result = new java.util.ArrayList<>();
		for (PointsRuleEntity rule : activeRules) {
			PointsTaskStatusVO vo = org.springblade.core.tool.utils.BeanUtil.copy(rule, PointsTaskStatusVO.class);
			if (vo == null) {
				continue;
			}
			String ruleCode = rule.getRuleCode();
			int todayCount = Long.valueOf(todayCountMap.getOrDefault(ruleCode, 0L)).intValue();
			int historyCount = Long.valueOf(historyCountMap.getOrDefault(ruleCode, 0L)).intValue();
			int todayPoints = todayPointsMap.getOrDefault(ruleCode, 0);
			vo.setCompletedToday(todayCount > 0 ? 1 : 0);
			vo.setCompletedHistory(historyCount > 0 ? 1 : 0);
			vo.setTodayGrantCount(todayCount);
			vo.setTodayGrantPoints(todayPoints);
			vo.setContinueDays(continueDays);
			vo.setMonthSigninDays(monthSigninDays);

			String code = Func.toStr(ruleCode).toUpperCase();
			if ("DAILY_SIGNIN".equals(code)) {
				vo.setProgressValue(continueDays);
				vo.setProgressTarget(1);
				vo.setTaskStatus(signedToday ? "DONE_TODAY" : "TODO");
			} else if ("SIGNIN_STREAK_7".equals(code)) {
				vo.setProgressValue(continueDays);
				vo.setProgressTarget(7);
				vo.setTaskStatus(signedToday && continueDays >= 7 ? "DONE_TODAY" : "PROGRESS");
			} else if ("SIGNIN_STREAK_30".equals(code)) {
				vo.setProgressValue(continueDays);
				vo.setProgressTarget(30);
				vo.setTaskStatus(signedToday && continueDays >= 30 ? "DONE_TODAY" : "PROGRESS");
			} else if (rule.getRequireFirstFlag() != null && rule.getRequireFirstFlag() == 1) {
				vo.setTaskStatus(historyCount > 0 ? "DONE_ONCE" : "TODO");
			} else if (rule.getLifecycleLimitCount() != null && rule.getLifecycleLimitCount() > 0) {
				vo.setProgressValue(historyCount);
				vo.setProgressTarget(rule.getLifecycleLimitCount());
				vo.setTaskStatus(historyCount >= rule.getLifecycleLimitCount() ? "DONE_ONCE" : "TODO");
			} else if (todayCount > 0) {
				vo.setTaskStatus("DONE_TODAY");
			} else {
				vo.setTaskStatus("TODO");
			}
			result.add(vo);
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String grantPointsByRule(Long userId, String ruleCode, String bizType, String bizId, String requestId, String remark) {
		if (userId == null) return "用户不存在";
		if (Func.isEmpty(ruleCode)) return "规则编码不能为空";
		if (Func.isEmpty(bizType)) return "业务类型不能为空";
		if (Func.isEmpty(bizId)) return "业务ID不能为空";
		String finalRequestId = Func.isEmpty(requestId) ? UUID.randomUUID().toString() : requestId;

		PointsRuleEntity rule = getOne(Wrappers.<PointsRuleEntity>lambdaQuery()
			.eq(PointsRuleEntity::getRuleCode, ruleCode)
			.eq(PointsRuleEntity::getIsDeleted, 0)
			.eq(PointsRuleEntity::getStatus, 1)
			.last("limit 1"));
		if (rule == null) return "积分规则未启用";

		PointsTaskLogEntity idempotent = pointsTaskLogService.getOne(Wrappers.<PointsTaskLogEntity>lambdaQuery()
			.eq(PointsTaskLogEntity::getRequestId, finalRequestId)
			.eq(PointsTaskLogEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (idempotent != null) return "发放成功+" + Math.max(rule.getGrantPoints(), 0) + "绿豆";

		PointsTaskLogEntity bizDup = pointsTaskLogService.getOne(Wrappers.<PointsTaskLogEntity>lambdaQuery()
			.eq(PointsTaskLogEntity::getUserId, userId)
			.eq(PointsTaskLogEntity::getRuleCode, ruleCode)
			.eq(PointsTaskLogEntity::getBizType, bizType)
			.eq(PointsTaskLogEntity::getBizId, bizId)
			.eq(PointsTaskLogEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (bizDup != null) return "已发放过该任务奖励";

		Date now = new Date();
		Date today = dayStart(now);
		int grantPoints = Func.toInt(rule.getGrantPoints(), 0);

		PointsDailyCounterEntity sceneCounter = pointsDailyCounterService.getOne(Wrappers.<PointsDailyCounterEntity>lambdaQuery()
			.eq(PointsDailyCounterEntity::getUserId, userId)
			.eq(PointsDailyCounterEntity::getStatDate, today)
			.eq(PointsDailyCounterEntity::getSceneType, Func.toStr(rule.getSceneType()))
			.eq(PointsDailyCounterEntity::getIsDeleted, 0)
			.last("limit 1"));
		int sceneCount = sceneCounter == null ? 0 : Func.toInt(sceneCounter.getGrantCount(), 0);
		int scenePoints = sceneCounter == null ? 0 : Func.toInt(sceneCounter.getGrantPoints(), 0);
		if (rule.getDailyLimitCount() != null && sceneCount >= rule.getDailyLimitCount()) {
			return "超出当日触发次数上限";
		}
		if (rule.getDailyLimitPoints() != null && (scenePoints + grantPoints) > rule.getDailyLimitPoints()) {
			return "超出当日场景积分上限";
		}

		int todayTotal = pointsDailyCounterService.list(Wrappers.<PointsDailyCounterEntity>lambdaQuery()
			.eq(PointsDailyCounterEntity::getUserId, userId)
			.eq(PointsDailyCounterEntity::getStatDate, today)
			.eq(PointsDailyCounterEntity::getIsDeleted, 0))
			.stream()
			.mapToInt(v -> Func.toInt(v.getGrantPoints(), 0))
			.sum();
		if (todayTotal + grantPoints > 500) {
			return "超出每日获取上限";
		}

		PointsAccountEntity account = pointsAccountService.getOne(Wrappers.<PointsAccountEntity>lambdaQuery()
			.eq(PointsAccountEntity::getUserId, userId)
			.eq(PointsAccountEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (account == null) {
			account = new PointsAccountEntity();
			account.setUserId(userId);
			account.setAvailablePoints(0);
			account.setFrozenPoints(0);
			account.setTotalEarnedPoints(0);
			account.setTotalSpentPoints(0);
			account.setGrowthLevel(0);
			account.setVersion(0);
			pointsAccountService.save(account);
		}

		int before = Func.toInt(account.getAvailablePoints(), 0);
		int earnedBefore = Func.toInt(account.getTotalEarnedPoints(), 0);
		account.setAvailablePoints(before + grantPoints);
		account.setTotalEarnedPoints(earnedBefore + grantPoints);
		account.setGrowthLevel(calcGrowthLevel(earnedBefore + grantPoints));
		pointsAccountService.updateById(account);

		PointsLedgerEntity ledger = new PointsLedgerEntity();
		ledger.setUserId(userId);
		ledger.setChangeType("INCOME");
		ledger.setChangePoints(grantPoints);
		ledger.setBeforePoints(before);
		ledger.setAfterPoints(before + grantPoints);
		ledger.setRuleCode(ruleCode);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setRemark(Func.isEmpty(remark) ? rule.getRuleName() : remark);
		ledger.setRequestId(finalRequestId);
		Calendar expireCal = Calendar.getInstance();
		expireCal.setTime(now);
		expireCal.add(Calendar.MONTH, 12);
		ledger.setExpiresAt(expireCal.getTime());
		pointsLedgerService.save(ledger);

		PointsTaskLogEntity taskLog = new PointsTaskLogEntity();
		taskLog.setRequestId(finalRequestId);
		taskLog.setUserId(userId);
		taskLog.setRuleCode(ruleCode);
		taskLog.setBizType(bizType);
		taskLog.setBizId(bizId);
		taskLog.setStatus(1);
		pointsTaskLogService.save(taskLog);

		if (sceneCounter == null) {
			sceneCounter = new PointsDailyCounterEntity();
			sceneCounter.setUserId(userId);
			sceneCounter.setStatDate(today);
			sceneCounter.setSceneType(rule.getSceneType());
			sceneCounter.setGrantCount(1);
			sceneCounter.setGrantPoints(grantPoints);
			pointsDailyCounterService.save(sceneCounter);
		} else {
			sceneCounter.setGrantCount(sceneCount + 1);
			sceneCounter.setGrantPoints(scenePoints + grantPoints);
			pointsDailyCounterService.updateById(sceneCounter);
		}

		return "发放成功+" + grantPoints + "绿豆";
	}

	private Date dayStart(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	private int calcGrowthLevel(int totalEarnedPoints) {
		if (totalEarnedPoints >= 20000) return 5;
		if (totalEarnedPoints >= 8000) return 4;
		if (totalEarnedPoints >= 3000) return 3;
		if (totalEarnedPoints >= 1000) return 2;
		if (totalEarnedPoints >= 100) return 1;
		return 0;
	}


	private String dayKey(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int y = c.get(Calendar.YEAR);
		int m = c.get(Calendar.MONTH) + 1;
		int d = c.get(Calendar.DAY_OF_MONTH);
		return y + String.format("%02d", m) + String.format("%02d", d);
	}

}

