package org.springblade.modules.usercoupon.service;

import lombok.RequiredArgsConstructor;
import org.springblade.common.utils.RedisUtils;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.coupontemplate.pojo.entity.CouponTemplateEntity;
import org.springblade.modules.coupontemplate.service.ICouponTemplateService;
import org.springblade.modules.usercoupon.pojo.dto.UserCouponVerifyConfirmRequest;
import org.springblade.modules.usercoupon.pojo.entity.UserCouponEntity;
import org.springblade.modules.usercoupon.pojo.vo.UserCouponVO;
import org.springblade.modules.venue.pojo.entity.VenueEntity;
import org.springblade.modules.venue.service.IVenueService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 优惠券二维码与核销统一业务服务。
 *
 * <p>所有小程序核销入口必须经过：核销员授权范围、动态二维码、扫码会话、
 * 分布式互斥锁和用户券状态校验。Controller 不再自行拼装另一套核销流程。</p>
 */
@Service
@RequiredArgsConstructor
public class CouponVerificationService {

	private static final String QR_TOKEN_PREFIX = "coupon:verify:token:";
	private static final String VERIFY_SESSION_PREFIX = "coupon:verify:session:";
	private static final String VERIFY_LOCK_PREFIX = "coupon:verify:lock:";
	private static final int QR_TOKEN_TTL_SECONDS = 90;
	private static final int VERIFY_SESSION_TTL_SECONDS = 300;
	private static final int VERIFY_LOCK_TTL_SECONDS = 30;

	private final IUserCouponService userCouponService;
	private final ICouponTemplateService couponTemplateService;
	private final IVenueService venueService;
	private final RedisUtils redisUtils;
	private final JdbcTemplate jdbcTemplate;

	public Map<String, Object> createQrToken(Long userCouponId, Long currentUserId) {
		UserCouponEntity coupon = requireCoupon(userCouponId);
		if (currentUserId == null || !currentUserId.equals(coupon.getUserId())) {
			throw new IllegalArgumentException("无权操作该优惠券");
		}
		validateUsable(coupon);

		String token = UUID.randomUUID().toString().replace("-", "");
		redisUtils.set(QR_TOKEN_PREFIX + token, String.valueOf(coupon.getId()), QR_TOKEN_TTL_SECONDS);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("qrToken", token);
		result.put("expiresIn", QR_TOKEN_TTL_SECONDS);
		result.put("refreshInSeconds", 60);
		result.put("expiresAt", new Date(System.currentTimeMillis() + QR_TOKEN_TTL_SECONDS * 1000L));
		return result;
	}

	public Map<String, Object> getVerifierPermission(Long verifierUserId) {
		List<Map<String, Object>> rows = loadEnabledScopes(verifierUserId);
		List<Map<String, Object>> scopes = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			String scopeType = upper(Func.toStr(row.get("scope_type"), ""));
			String scopeRefId = Func.toStr(row.get("scope_ref_id"), "");
			Map<String, Object> scope = new LinkedHashMap<>();
			scope.put("scopeType", scopeType);
			scope.put("scopeRefId", scopeRefId);
			scope.put("scopeName", resolveScopeName(scopeType, scopeRefId));
			scopes.add(scope);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("canVerify", !scopes.isEmpty());
		result.put("scopes", scopes);
		result.put("venueName", scopes.stream()
			.map(item -> Func.toStr(item.get("scopeName"), ""))
			.filter(Func::isNotBlank)
			.findFirst()
			.orElse(""));
		return result;
	}

	public UserCouponVO scan(String qrToken, Long verifierUserId) {
		ensureHasAnyScope(verifierUserId);
		if (Func.isBlank(qrToken)) {
			throw new IllegalArgumentException("二维码内容不能为空");
		}
		Object cached = redisUtils.get(QR_TOKEN_PREFIX + qrToken);
		if (cached == null) {
			throw new IllegalArgumentException("二维码已失效，请让用户刷新后重试");
		}

		Long couponId = parseLong(cached);
		UserCouponEntity coupon = requireCoupon(couponId);
		validateUsable(coupon);
		CouponTemplateEntity template = requireTemplate(coupon.getCouponTemplateId());
		ensureScopeMatches(verifierUserId, template);

		redisUtils.set(sessionKey(verifierUserId, couponId), "1", VERIFY_SESSION_TTL_SECONDS);
		UserCouponVO detail = userCouponService.buildCouponDetail(couponId);
		if (detail != null) {
			detail.setVerifyRecords(userCouponService.getVerifyRecords(couponId));
		}
		return detail;
	}

	public Map<String, Object> confirm(UserCouponVerifyConfirmRequest request, Long verifierUserId) {
		ensureHasAnyScope(verifierUserId);
		if (request == null || request.getUserCouponId() == null) {
			throw new IllegalArgumentException("优惠券参数不能为空");
		}

		Long couponId = request.getUserCouponId();
		String sessionKey = sessionKey(verifierUserId, couponId);
		if (redisUtils.get(sessionKey) == null) {
			throw new IllegalArgumentException("请重新扫码后再核销");
		}

		String lockKey = VERIFY_LOCK_PREFIX + couponId;
		Boolean locked = redisUtils.getRedisTemplate().opsForValue()
			.setIfAbsent(lockKey, String.valueOf(verifierUserId), VERIFY_LOCK_TTL_SECONDS, TimeUnit.SECONDS);
		if (!Boolean.TRUE.equals(locked)) {
			throw new IllegalArgumentException("该优惠券正在核销，请勿重复提交");
		}

		try {
			UserCouponEntity coupon = requireCoupon(couponId);
			validateUsable(coupon);
			CouponTemplateEntity template = requireTemplate(coupon.getCouponTemplateId());
			ensureScopeMatches(verifierUserId, template);

			boolean fullUse = !"PARTIAL".equalsIgnoreCase(Func.toStr(request.getVerifyMode(), "FULL"));
			String message = userCouponService.useCouponById(
				couponId,
				null,
				verifierUserId,
				fullUse,
				Func.toInt(request.getConsumeDurationMinutes(), 0),
				Func.toInt(request.getConsumeTimes(), 0)
			);
			if (!"核销成功".equals(message)) {
				throw new IllegalArgumentException(message);
			}

			redisUtils.delete(sessionKey);
			UserCouponVO detail = userCouponService.buildCouponDetail(couponId);
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("success", true);
			result.put("message", message);
			result.put("userCouponId", couponId);
			result.put("couponStatus", detail == null ? "" : detail.getCouponStatus());
			result.put("remainDurationMinutes", detail == null ? 0 : detail.getRemainDurationMinutes());
			result.put("remainTimes", detail == null ? 0 : detail.getRemainTimes());
			return result;
		} finally {
			redisUtils.delete(lockKey);
		}
	}

	public List<Map<String, Object>> getOwnerVerifyRecords(Long userCouponId, Long currentUserId) {
		UserCouponEntity coupon = requireCoupon(userCouponId);
		if (currentUserId == null || !currentUserId.equals(coupon.getUserId())) {
			throw new IllegalArgumentException("无权查看核销记录");
		}
		return userCouponService.getVerifyRecords(userCouponId);
	}

	private UserCouponEntity requireCoupon(Long couponId) {
		if (couponId == null) {
			throw new IllegalArgumentException("优惠券不存在");
		}
		UserCouponEntity coupon = userCouponService.getById(couponId);
		if (coupon == null || Func.equals(coupon.getIsDeleted(), 1)) {
			throw new IllegalArgumentException("优惠券不存在");
		}
		return coupon;
	}

	private CouponTemplateEntity requireTemplate(Long templateId) {
		CouponTemplateEntity template = couponTemplateService.getById(templateId);
		if (template == null || Func.equals(template.getIsDeleted(), 1)) {
			throw new IllegalArgumentException("优惠券模板不存在或已停用");
		}
		return template;
	}

	private void validateUsable(UserCouponEntity coupon) {
		String status = upper(Func.toStr(coupon.getCouponStatus(), ""));
		if (!"UNUSED".equals(status) && !"PARTIAL_USED".equals(status)) {
			throw new IllegalArgumentException("当前券状态不可核销");
		}
		Date now = new Date();
		if (coupon.getValidStartAt() != null && coupon.getValidStartAt().after(now)) {
			throw new IllegalArgumentException("优惠券尚未生效");
		}
		if (coupon.getValidEndAt() != null && coupon.getValidEndAt().before(now)) {
			throw new IllegalArgumentException("优惠券已过期");
		}
	}

	private void ensureHasAnyScope(Long verifierUserId) {
		if (loadEnabledScopes(verifierUserId).isEmpty()) {
			throw new IllegalArgumentException("当前账号暂无核销权限");
		}
	}

	private void ensureScopeMatches(Long verifierUserId, CouponTemplateEntity template) {
		String scopeType = upper(Func.toStr(template.getScopeType(), "ALL"));
		String scopeRefId = Func.toStr(template.getScopeRefId(), "ALL");
		Integer count = jdbcTemplate.queryForObject(
			"SELECT COUNT(1) FROM coupon_verifier_scope " +
				"WHERE verifier_user_id = ? AND status = 1 AND is_deleted = 0 " +
				"AND ((scope_type = 'ALL' AND scope_ref_id = 'ALL') OR (scope_type = ? AND scope_ref_id = ?))",
			Integer.class,
			verifierUserId,
			scopeType,
			scopeRefId
		);
		if (count == null || count <= 0) {
			throw new IllegalArgumentException("该优惠券不在当前账号的核销范围内");
		}
	}

	private List<Map<String, Object>> loadEnabledScopes(Long verifierUserId) {
		if (verifierUserId == null) {
			return Collections.emptyList();
		}
		return jdbcTemplate.queryForList(
			"SELECT scope_type, scope_ref_id FROM coupon_verifier_scope " +
				"WHERE verifier_user_id = ? AND status = 1 AND is_deleted = 0 ORDER BY id ASC",
			verifierUserId
		);
	}

	private String resolveScopeName(String scopeType, String scopeRefId) {
		if ("ALL".equals(scopeType)) {
			return "全部授权范围";
		}
		if ("VENUE".equals(scopeType)) {
			Long venueId = parseLong(scopeRefId);
			VenueEntity venue = venueId == null ? null : venueService.getById(venueId);
			return venue == null ? "场馆" : Func.toStr(venue.getName(), "场馆");
		}
		Map<String, String> labels = new HashMap<>();
		labels.put("TRAINING", "培训课程");
		labels.put("COURSE", "培训课程");
		labels.put("CAMP", "户外营地");
		labels.put("GOODS", "商城商品");
		return labels.getOrDefault(scopeType, scopeType);
	}

	private String sessionKey(Long verifierUserId, Long couponId) {
		return VERIFY_SESSION_PREFIX + verifierUserId + ":" + couponId;
	}

	private Long parseLong(Object value) {
		try {
			return value == null ? null : Long.valueOf(String.valueOf(value));
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private String upper(String value) {
		return Func.toStr(value, "").toUpperCase(Locale.ROOT);
	}
}
