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
package org.springblade.modules.usercoupon.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springblade.common.cache.SysCache;
import org.springblade.common.utils.RedisUtils;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.coupontemplate.pojo.entity.CouponTemplateEntity;
import org.springblade.modules.coupontemplate.service.ICouponTemplateService;
import org.springblade.modules.usercoupon.pojo.entity.UserCouponEntity;
import org.springblade.modules.usercoupon.pojo.dto.UserCouponVerifyConfirmRequest;
import org.springblade.modules.usercoupon.pojo.vo.UserCouponVO;
import org.springblade.modules.usercoupon.excel.UserCouponExcel;
import org.springblade.modules.usercoupon.mapper.UserCouponMapper;
import org.springblade.modules.usercoupon.service.IUserCouponService;
import org.springblade.modules.couponverifylog.pojo.entity.CouponVerifyLogEntity;
import org.springblade.modules.couponverifylog.service.ICouponVerifyLogService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.venue.pojo.entity.VenueEntity;
import org.springblade.modules.venue.service.IVenueService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;

import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户认证类型表 服务实现类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl extends BaseServiceImpl<UserCouponMapper, UserCouponEntity> implements IUserCouponService {

	private static final String VERIFY_TOKEN_PREFIX = "coupon:verify:token:";
	private static final int VERIFY_TOKEN_TTL_SECONDS = 90;

	private final ICouponVerifyLogService couponVerifyLogService;
	private final ICouponTemplateService couponTemplateService;
	private final IVenueService venueService;
	private final IUserService userService;
	private final RedisUtils redisUtils;
	private final ObjectMapper objectMapper;

	@Override
	public IPage<UserCouponVO> selectUserCouponPage(IPage<UserCouponVO> page, UserCouponVO userCoupon) {
		return page.setRecords(baseMapper.selectUserCouponPage(page, userCoupon));
	}


	@Override
	public List<UserCouponExcel> exportUserCoupon(Wrapper<UserCouponEntity> queryWrapper) {
		List<UserCouponExcel> userCouponList = baseMapper.exportUserCoupon(queryWrapper);
		//userCouponList.forEach(userCoupon -> {
		//	userCoupon.setTypeName(DictCache.getValue(DictEnum.YES_NO, UserCoupon.getType()));
		//});
		return userCouponList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String useCoupon(String couponNo, String orderNo, Long merchantUserId) {
		UserCouponEntity coupon = this.getOne(Wrappers.<UserCouponEntity>lambdaQuery()
			.eq(UserCouponEntity::getCouponNo, couponNo)
			.eq(UserCouponEntity::getIsDeleted, 0));
		if (coupon == null) return "券不存在";
		if (!"UNUSED".equalsIgnoreCase(coupon.getCouponStatus()) && !"LOCKED".equalsIgnoreCase(coupon.getCouponStatus())) return "券状态不可核销";
		if (coupon.getValidEndAt() != null && coupon.getValidEndAt().before(new Date())) return "券已过期";

		String nextStatus = "USED";
		if (coupon.getRemainTimes() != null && coupon.getRemainTimes() > 0) {
			coupon.setRemainTimes(coupon.getRemainTimes() - 1);
			nextStatus = coupon.getRemainTimes() > 0 ? "UNUSED" : "USED";
		}
		coupon.setCouponStatus(nextStatus);
		coupon.setUsedAt(new Date());
		coupon.setUsedOrderNo(orderNo);
		coupon.setVerifyMerchantUserId(merchantUserId);
		coupon.setVerifyAt(new Date());
		if (coupon.getRemainDurationMinutes() != null && coupon.getRemainDurationMinutes() > 0) coupon.setRemainDurationMinutes(0);
		this.updateById(coupon);

		CouponVerifyLogEntity log = new CouponVerifyLogEntity();
		log.setUserCouponId(coupon.getId());
		log.setUserId(coupon.getUserId());
		log.setMerchantUserId(merchantUserId);
		log.setTemplateId(coupon.getCouponTemplateId());
		log.setCouponNo(couponNo);
		log.setVerifyChannel("APP");
		log.setVerifyResult(1);
		log.setVerifyStatus("FINISHED");
		log.setOrderNo(orderNo);
		couponVerifyLogService.save(log);
		return "核销成功";
	}

	@Override
	public String releaseCoupon(String couponNo) {
		UserCouponEntity coupon = this.getOne(Wrappers.<UserCouponEntity>lambdaQuery()
			.eq(UserCouponEntity::getCouponNo, couponNo)
			.eq(UserCouponEntity::getIsDeleted, 0));
		if (coupon == null) return "券不存在";
		if (!"LOCKED".equalsIgnoreCase(coupon.getCouponStatus())) return "当前状态无需释放";
		coupon.setCouponStatus("UNUSED");
		coupon.setLockedOrderNo(null);
		this.updateById(coupon);
		return "释放成功";
	}

	@Override
	public UserCouponVO buildCouponDetail(Long userCouponId) {
		if (userCouponId == null) {
			return null;
		}
		UserCouponEntity coupon = this.getById(userCouponId);
		return enrichCoupon(coupon, null, null, true);
	}

	@Override
	public List<UserCouponVO> buildCouponList(List<UserCouponEntity> coupons) {
		if (Func.isEmpty(coupons)) {
			return Collections.emptyList();
		}
		Map<Long, CouponTemplateEntity> templateMap = loadTemplateMap(coupons);
		Map<String, String> venueMap = loadVenueMap(templateMap.values());
		return coupons.stream()
			.map(coupon -> enrichCoupon(coupon, templateMap, venueMap, false))
			.collect(Collectors.toList());
	}

	@Override
	public Map<String, Object> getQrCodeToken(Long userCouponId, Long currentUserId) {
		UserCouponEntity coupon = requireCoupon(userCouponId);
		if (currentUserId != null && coupon.getUserId() != null && !currentUserId.equals(coupon.getUserId())) {
			throw new IllegalArgumentException("无权查看该优惠券二维码");
		}
		validateCouponForVerify(coupon);
		String token = UUID.randomUUID().toString().replace("-", "");
		Map<String, Object> tokenPayload = new HashMap<>(4);
		tokenPayload.put("userCouponId", coupon.getId());
		tokenPayload.put("couponNo", coupon.getCouponNo());
		tokenPayload.put("userId", coupon.getUserId());
		try {
			redisUtils.set(VERIFY_TOKEN_PREFIX + token, objectMapper.writeValueAsString(tokenPayload), VERIFY_TOKEN_TTL_SECONDS);
		} catch (Exception e) {
			throw new IllegalStateException("二维码令牌生成失败");
		}
		Map<String, Object> result = new HashMap<>(4);
		result.put("qrToken", token);
		result.put("expiresIn", VERIFY_TOKEN_TTL_SECONDS);
		result.put("refreshInSeconds", VERIFY_TOKEN_TTL_SECONDS - 10);
		result.put("expiresAt", new Date(System.currentTimeMillis() + VERIFY_TOKEN_TTL_SECONDS * 1000L));
		return result;
	}

	@Override
	public List<Map<String, Object>> getVerifyRecords(Long userCouponId) {
		if (userCouponId == null) {
			return Collections.emptyList();
		}
		List<CouponVerifyLogEntity> logs = couponVerifyLogService.list(new LambdaQueryWrapper<CouponVerifyLogEntity>()
			.eq(CouponVerifyLogEntity::getUserCouponId, userCouponId)
			.eq(CouponVerifyLogEntity::getIsDeleted, 0)
			.orderByDesc(CouponVerifyLogEntity::getCreateTime));
		if (Func.isEmpty(logs)) {
			return Collections.emptyList();
		}
		UserCouponEntity coupon = this.getById(userCouponId);
		String defaultVenueName = loadVenueNameByCoupon(coupon);
		return logs.stream().map(log -> buildVerifyRecord(log, defaultVenueName)).collect(Collectors.toList());
	}

	@Override
	public Map<String, Object> getVerifyPermission(Long merchantUserId) {
		Map<String, Object> result = new HashMap<>(4);
		result.put("canVerify", hasVerifyPermission(merchantUserId));
		result.put("merchantUserId", merchantUserId);
		result.put("venueName", "");
		return result;
	}

	@Override
	public UserCouponVO scanVerify(String qrToken, Long merchantUserId) {
		ensureVerifyPermission(merchantUserId);
		if (Func.isBlank(qrToken)) {
			throw new IllegalArgumentException("二维码内容不能为空");
		}
		Map<String, Object> tokenPayload = readTokenPayload(qrToken);
		Long userCouponId = Func.toLong(tokenPayload.get("userCouponId"));
		UserCouponVO detail = buildCouponDetail(userCouponId);
		if (detail == null) {
			throw new IllegalArgumentException("优惠券不存在");
		}
		validateCouponForVerify(detail);
		detail.setVerifyRecords(getVerifyRecords(userCouponId));
		return detail;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> confirmVerify(UserCouponVerifyConfirmRequest request, Long merchantUserId) {
		ensureVerifyPermission(merchantUserId);
		if (request == null || request.getUserCouponId() == null) {
			throw new IllegalArgumentException("优惠券参数不能为空");
		}
		UserCouponEntity coupon = requireCoupon(request.getUserCouponId());
		validateCouponForVerify(coupon);

		CouponTemplateEntity template = couponTemplateService.getById(coupon.getCouponTemplateId());
		String couponType = upper(Func.toStr(template != null ? template.getCouponType() : null, ""));
		String verifyMode = upper(Func.toStr(request.getVerifyMode(), "FULL"));
		int totalDuration = safeInt(coupon.getRemainDurationMinutes());
		int totalTimes = safeInt(coupon.getRemainTimes());
		int consumeDuration = 0;
		int consumeTimes = 0;
		int afterRemainDuration = totalDuration;
		int afterRemainTimes = totalTimes;
		String nextStatus = "USED";

		if ("PARTIAL".equals(verifyMode) && "DURATION".equals(couponType)) {
			consumeDuration = safeInt(request.getConsumeDurationMinutes());
			if (consumeDuration <= 0) {
				throw new IllegalArgumentException("请输入本次核销分钟数");
			}
			if (consumeDuration > totalDuration) {
				throw new IllegalArgumentException("核销分钟数不能超过剩余时长");
			}
			afterRemainDuration = totalDuration - consumeDuration;
			nextStatus = afterRemainDuration > 0 ? "PARTIAL_USED" : "USED";
		} else if ("PARTIAL".equals(verifyMode) && "TIMES".equals(couponType)) {
			consumeTimes = safeInt(request.getConsumeTimes());
			if (consumeTimes <= 0) {
				throw new IllegalArgumentException("请输入本次核销次数");
			}
			if (consumeTimes > totalTimes) {
				throw new IllegalArgumentException("核销次数不能超过剩余次数");
			}
			afterRemainTimes = totalTimes - consumeTimes;
			nextStatus = afterRemainTimes > 0 ? "PARTIAL_USED" : "USED";
		} else {
			consumeDuration = totalDuration;
			consumeTimes = totalTimes > 0 ? totalTimes : ("TIMES".equals(couponType) ? 1 : 0);
			afterRemainDuration = 0;
			afterRemainTimes = 0;
			nextStatus = "USED";
		}

		coupon.setRemainDurationMinutes(afterRemainDuration);
		coupon.setRemainTimes(afterRemainTimes);
		coupon.setCouponStatus(nextStatus);
		coupon.setUsedAt(new Date());
		coupon.setVerifyMerchantUserId(merchantUserId);
		coupon.setVerifyAt(new Date());
		this.updateById(coupon);

		CouponVerifyLogEntity log = new CouponVerifyLogEntity();
		log.setUserCouponId(coupon.getId());
		log.setUserId(coupon.getUserId());
		log.setMerchantUserId(merchantUserId);
		log.setTemplateId(coupon.getCouponTemplateId());
		log.setCouponNo(coupon.getCouponNo());
		log.setVerifyChannel("APP");
		log.setVerifyResult(1);
		log.setVerifyStatus("FINISHED");
		log.setExtJson(writeExtJson(consumeDuration, consumeTimes, afterRemainDuration, afterRemainTimes, verifyMode));
		couponVerifyLogService.save(log);

		Map<String, Object> result = new HashMap<>(6);
		result.put("success", true);
		result.put("code", 200);
		result.put("msg", "核销成功");
		result.put("couponStatus", nextStatus);
		result.put("remainText", buildRemainText(couponType, afterRemainDuration, afterRemainTimes, nextStatus));
		result.put("verifyRecord", buildVerifyRecord(log, loadVenueNameByCoupon(coupon)));
		return result;
	}

	private UserCouponVO enrichCoupon(UserCouponEntity coupon, Map<Long, CouponTemplateEntity> templateMap, Map<String, String> venueMap, boolean withRecords) {
		if (coupon == null) {
			return null;
		}
		CouponTemplateEntity template = templateMap != null
			? templateMap.get(coupon.getCouponTemplateId())
			: couponTemplateService.getById(coupon.getCouponTemplateId());
		Map<String, Object> extMap = parseExtJson(template != null ? template.getExtJson() : null);
		UserCouponVO vo = new UserCouponVO();
		vo.setId(coupon.getId());
		vo.setUserId(coupon.getUserId());
		vo.setCouponTemplateId(coupon.getCouponTemplateId());
		vo.setCouponNo(coupon.getCouponNo());
		vo.setCouponStatus(normalizeCouponStatus(coupon));
		vo.setRemainDurationMinutes(coupon.getRemainDurationMinutes());
		vo.setRemainTimes(coupon.getRemainTimes());
		vo.setValidStartAt(coupon.getValidStartAt());
		vo.setValidEndAt(coupon.getValidEndAt());
		vo.setLockedOrderNo(coupon.getLockedOrderNo());
		vo.setUsedOrderNo(coupon.getUsedOrderNo());
		vo.setUsedAt(coupon.getUsedAt());
		vo.setVerifyMerchantUserId(coupon.getVerifyMerchantUserId());
		vo.setVerifyAt(coupon.getVerifyAt());
		vo.setCreateTime(coupon.getCreateTime());
		vo.setUpdateTime(coupon.getUpdateTime());
		vo.setCreateUser(coupon.getCreateUser());
		vo.setUpdateUser(coupon.getUpdateUser());
		vo.setStatus(coupon.getStatus());
		vo.setTenantId(coupon.getTenantId());
		vo.setCreateDept(coupon.getCreateDept());
		vo.setIsDeleted(coupon.getIsDeleted());
		if (template != null) {
			vo.setCouponName(template.getCouponName());
			vo.setCouponType(template.getCouponType());
			vo.setBenefitMode(template.getBenefitMode());
			vo.setDurationMinutes(template.getDurationMinutes());
			vo.setTotalTimes(template.getTotalTimes());
			vo.setScopeType(template.getScopeType());
			vo.setScopeRefId(template.getScopeRefId());
			vo.setAcquireType(template.getAcquireType());
			vo.setCostPoints(template.getCostPoints());
			vo.setTotalDurationMinutes(template.getDurationMinutes());
		}
		String venueName = "";
		if ("VENUE".equalsIgnoreCase(vo.getScopeType()) && Func.isNotBlank(vo.getScopeRefId())) {
			venueName = venueMap != null ? venueMap.getOrDefault(vo.getScopeRefId(), "") : loadVenueName(vo.getScopeRefId());
		}
		vo.setVenueName(venueName);
		vo.setUsageNotice(firstNonBlank(
			Func.toStr(extMap.get("usageNotice")),
			Func.toStr(extMap.get("usage_notice")),
			Func.toStr(extMap.get("notice"))
		));
		User user = coupon.getUserId() == null ? null : userService.getById(coupon.getUserId());
		if (user != null) {
			vo.setUserName(firstNonBlank(user.getRealName(), user.getName(), user.getAccount()));
			vo.setNickname(user.getName());
			vo.setPhoneMask(maskPhone(user.getPhone()));
		}
		if (withRecords) {
			vo.setVerifyRecords(getVerifyRecords(coupon.getId()));
		}
		return vo;
	}

	private Map<Long, CouponTemplateEntity> loadTemplateMap(List<UserCouponEntity> coupons) {
		List<Long> templateIds = coupons.stream()
			.map(UserCouponEntity::getCouponTemplateId)
			.filter(Func::isNotEmpty)
			.distinct()
			.collect(Collectors.toList());
		if (Func.isEmpty(templateIds)) {
			return Collections.emptyMap();
		}
		return couponTemplateService.listByIds(templateIds).stream()
			.collect(Collectors.toMap(CouponTemplateEntity::getId, item -> item, (left, right) -> left));
	}

	private Map<String, String> loadVenueMap(java.util.Collection<CouponTemplateEntity> templates) {
		List<Long> venueIds = templates.stream()
			.filter(item -> item != null && "VENUE".equalsIgnoreCase(item.getScopeType()) && Func.isNotBlank(item.getScopeRefId()))
			.map(item -> Func.toLong(item.getScopeRefId()))
			.filter(Func::isNotEmpty)
			.distinct()
			.collect(Collectors.toList());
		if (Func.isEmpty(venueIds)) {
			return Collections.emptyMap();
		}
		return venueService.listByIds(venueIds).stream()
			.collect(Collectors.toMap(item -> String.valueOf(item.getId()), VenueEntity::getName, (left, right) -> left));
	}

	private UserCouponEntity requireCoupon(Long userCouponId) {
		UserCouponEntity coupon = this.getById(userCouponId);
		if (coupon == null || Func.equals(coupon.getIsDeleted(), 1)) {
			throw new IllegalArgumentException("优惠券不存在");
		}
		return coupon;
	}

	private void validateCouponForVerify(UserCouponEntity coupon) {
		String status = upper(normalizeCouponStatus(coupon));
		if (!"UNUSED".equals(status) && !"LOCKED".equals(status) && !"PARTIAL_USED".equals(status)) {
			throw new IllegalArgumentException("券状态不可核销");
		}
		if (coupon.getValidEndAt() != null && coupon.getValidEndAt().before(new Date())) {
			throw new IllegalArgumentException("券已过期");
		}
	}

	private String normalizeCouponStatus(UserCouponEntity coupon) {
		String status = upper(Func.toStr(coupon.getCouponStatus(), "UNUSED"));
		if ("UNUSED".equals(status)) {
			if (safeInt(coupon.getRemainDurationMinutes()) > 0 && coupon.getUsedAt() != null) {
				return "PARTIAL_USED";
			}
			if (safeInt(coupon.getRemainTimes()) > 0 && coupon.getUsedAt() != null) {
				return "PARTIAL_USED";
			}
		}
		return status;
	}

	private Map<String, Object> buildVerifyRecord(CouponVerifyLogEntity log, String defaultVenueName) {
		Map<String, Object> record = new HashMap<>(8);
		record.put("id", log.getId());
		record.put("verifyAt", log.getCreateTime());
		record.put("createTime", log.getCreateTime());
		record.put("verifyVenueName", defaultVenueName);
		record.put("venueName", defaultVenueName);
		Map<String, Object> extMap = parseExtJson(log.getExtJson());
		record.put("consumeDurationMinutes", safeInt(extMap.get("consumeDurationMinutes")));
		record.put("consumeTimes", safeInt(extMap.get("consumeTimes")));
		record.put("afterRemainDurationMinutes", safeInt(extMap.get("afterRemainDurationMinutes")));
		record.put("afterRemainTimes", safeInt(extMap.get("afterRemainTimes")));
		record.put("remark", Func.toStr(extMap.get("remark"), ""));
		return record;
	}

	private String buildRemainText(String couponType, int remainDuration, int remainTimes, String couponStatus) {
		if ("USED".equalsIgnoreCase(couponStatus)) {
			return "已全部核销";
		}
		if ("DURATION".equalsIgnoreCase(couponType)) {
			return "剩余 " + remainDuration + " 分钟";
		}
		if ("TIMES".equalsIgnoreCase(couponType)) {
			return "剩余 " + remainTimes + " 次";
		}
		return "整张券待核销";
	}

	private String writeExtJson(int consumeDuration, int consumeTimes, int afterRemainDuration, int afterRemainTimes, String verifyMode) {
		Map<String, Object> extMap = new HashMap<>(8);
		extMap.put("consumeDurationMinutes", consumeDuration);
		extMap.put("consumeTimes", consumeTimes);
		extMap.put("afterRemainDurationMinutes", afterRemainDuration);
		extMap.put("afterRemainTimes", afterRemainTimes);
		extMap.put("verifyMode", verifyMode);
		extMap.put("remark", "FULL".equalsIgnoreCase(verifyMode) ? "整张核销" : "部分核销");
		try {
			return objectMapper.writeValueAsString(extMap);
		} catch (Exception e) {
			return "{}";
		}
	}

	private Map<String, Object> readTokenPayload(String qrToken) {
		Object cached = redisUtils.get(VERIFY_TOKEN_PREFIX + qrToken);
		if (cached == null) {
			throw new IllegalArgumentException("二维码已失效，请让用户刷新后重试");
		}
		try {
			return objectMapper.readValue(String.valueOf(cached), new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			throw new IllegalArgumentException("二维码数据异常");
		}
	}

	private Map<String, Object> parseExtJson(String extJson) {
		if (Func.isBlank(extJson)) {
			return Collections.emptyMap();
		}
		try {
			return objectMapper.readValue(extJson, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			return Collections.emptyMap();
		}
	}

	private boolean hasVerifyPermission(Long merchantUserId) {
		if (merchantUserId == null) {
			return false;
		}
		if (Func.equals(merchantUserId, 1L)) {
			return true;
		}
		User user = userService.getById(merchantUserId);
		if (user == null) {
			return false;
		}
		String identityName = upper(firstNonBlank(user.getMainIdentityCode(), user.getMainIdentityName()));
		if (identityName.contains("VENUE") || identityName.contains("MERCHANT") || identityName.contains("场馆") || identityName.contains("商家")) {
			return true;
		}
		List<String> roleAliases = SysCache.getRoleAliases(user.getRoleId());
		List<String> roleNames = SysCache.getRoleNames(user.getRoleId());
		return containsVerifyRole(roleAliases) || containsVerifyRole(roleNames);
	}

	private boolean containsVerifyRole(List<String> roles) {
		if (Func.isEmpty(roles)) {
			return false;
		}
		for (String role : roles) {
			String upperRole = upper(role);
			if (upperRole.contains("ADMIN") || upperRole.contains("MERCHANT") || upperRole.contains("VENUE") || upperRole.contains("SHOP")
				|| upperRole.contains("场馆") || upperRole.contains("商家") || upperRole.contains("核销")) {
				return true;
			}
		}
		return false;
	}

	private void ensureVerifyPermission(Long merchantUserId) {
		if (!hasVerifyPermission(merchantUserId)) {
			throw new IllegalArgumentException("当前账号暂无核销权限");
		}
	}

	private String loadVenueNameByCoupon(UserCouponEntity coupon) {
		if (coupon == null || coupon.getCouponTemplateId() == null) {
			return "";
		}
		CouponTemplateEntity template = couponTemplateService.getById(coupon.getCouponTemplateId());
		if (template == null || !"VENUE".equalsIgnoreCase(template.getScopeType())) {
			return "";
		}
		return loadVenueName(template.getScopeRefId());
	}

	private String loadVenueName(String scopeRefId) {
		Long venueId = Func.toLong(scopeRefId);
		if (venueId == null) {
			return "";
		}
		VenueEntity venue = venueService.getById(venueId);
		return venue == null ? "" : Func.toStr(venue.getName(), "");
	}

	private int safeInt(Object value) {
		return Func.toInt(value, 0);
	}

	private String upper(String value) {
		return Func.toStr(value, "").toUpperCase(Locale.ROOT);
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			if (Func.isNotBlank(value)) {
				return value;
			}
		}
		return "";
	}

	private String maskPhone(String phone) {
		if (Func.isBlank(phone) || phone.length() < 7) {
			return Func.toStr(phone, "");
		}
		return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
	}

}

