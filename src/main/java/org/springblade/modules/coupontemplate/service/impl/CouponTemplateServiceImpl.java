package org.springblade.modules.coupontemplate.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.couponreceivelog.pojo.entity.CouponReceiveLogEntity;
import org.springblade.modules.couponreceivelog.service.ICouponReceiveLogService;
import org.springblade.modules.coupontemplate.excel.CouponTemplateExcel;
import org.springblade.modules.coupontemplate.mapper.CouponTemplateMapper;
import org.springblade.modules.coupontemplate.pojo.entity.CouponTemplateEntity;
import org.springblade.modules.coupontemplate.pojo.vo.CouponTemplateVO;
import org.springblade.modules.coupontemplate.service.ICouponTemplateService;
import org.springblade.modules.pointsaccount.pojo.entity.PointsAccountEntity;
import org.springblade.modules.pointsaccount.service.IPointsAccountService;
import org.springblade.modules.pointsledger.pojo.entity.PointsLedgerEntity;
import org.springblade.modules.pointsledger.service.IPointsLedgerService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.usercoupon.pojo.entity.UserCouponEntity;
import org.springblade.modules.usercoupon.service.IUserCouponService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** 优惠券模板与领取服务。 */
@Service
@RequiredArgsConstructor
public class CouponTemplateServiceImpl extends BaseServiceImpl<CouponTemplateMapper, CouponTemplateEntity> implements ICouponTemplateService {

	private static final String VALID_TYPE_FIXED = "FIXED";
	private static final String VALID_TYPE_RELATIVE = "RELATIVE";

	private final IUserCouponService userCouponService;
	private final ICouponReceiveLogService couponReceiveLogService;
	private final IPointsAccountService pointsAccountService;
	private final IPointsLedgerService pointsLedgerService;
	private final IUserService userService;

	@Override
	public IPage<CouponTemplateVO> selectCouponTemplatePage(IPage<CouponTemplateVO> page, CouponTemplateVO couponTemplate) {
		return page.setRecords(baseMapper.selectCouponTemplatePage(page, couponTemplate));
	}

	@Override
	public List<CouponTemplateExcel> exportCouponTemplate(Wrapper<CouponTemplateEntity> queryWrapper) {
		return baseMapper.exportCouponTemplate(queryWrapper);
	}

	@Override
	@Transactional(readOnly = true)
	public String receiveCheck(Long templateId, Long userId) {
		if (userId == null || userId <= 0) return "请先登录";
		CouponTemplateEntity template = this.getById(templateId);
		String eligibility = eligibilityError(template, userId);
		if (eligibility != null) return eligibility;
		long receiveCount = countReceived(userId, templateId);
		if (template.getPerUserLimit() != null && template.getPerUserLimit() > 0 && receiveCount >= template.getPerUserLimit()) {
			return "超过每人限领次数";
		}
		return "可领取";
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String receive(Long templateId, String requestId, Long userId) {
		if (userId == null || userId <= 0) return "请先登录";
		if (Func.isBlank(requestId)) return "缺少领取请求号，请刷新页面后重试";
		String effectiveRequestId = requestId.trim();
		if (effectiveRequestId.length() > 64) return "领取请求号不正确";

		CouponReceiveLogEntity idempotent = couponReceiveLogService.getOne(Wrappers.<CouponReceiveLogEntity>lambdaQuery()
			.eq(CouponReceiveLogEntity::getUserId, userId)
			.eq(CouponReceiveLogEntity::getRequestId, effectiveRequestId)
			.eq(CouponReceiveLogEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (idempotent != null && Func.equals(idempotent.getStatus(), 1)) return "领取成功";

		CouponTemplateEntity template = this.getOne(Wrappers.<CouponTemplateEntity>lambdaQuery()
			.eq(CouponTemplateEntity::getId, templateId)
			.eq(CouponTemplateEntity::getIsDeleted, 0)
			.last("FOR UPDATE"));
		String eligibility = eligibilityError(template, userId);
		if (eligibility != null) return eligibility;

		long receiveCount = countReceived(userId, templateId);
		if (template.getPerUserLimit() != null && template.getPerUserLimit() > 0 && receiveCount >= template.getPerUserLimit()) {
			return "超过每人限领次数";
		}

		int costPoints = "POINTS_EXCHANGE".equalsIgnoreCase(template.getAcquireType())
			? Math.max(template.getCostPoints() == null ? 0 : template.getCostPoints(), 0) : 0;
		PointsAccountEntity account = pointsAccountService.getOne(Wrappers.<PointsAccountEntity>lambdaQuery()
			.eq(PointsAccountEntity::getUserId, userId)
			.eq(PointsAccountEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (costPoints > 0 && (account == null || account.getAvailablePoints() == null || account.getAvailablePoints() < costPoints)) {
			return "绿豆不足";
		}

		ValidityWindow validity = resolveValidity(template, new Date());

		boolean reserved = this.update(Wrappers.<CouponTemplateEntity>lambdaUpdate()
			.eq(CouponTemplateEntity::getId, templateId)
			.eq(CouponTemplateEntity::getStatus, 1)
			.gt(CouponTemplateEntity::getRemainStock, 0)
			.setSql("remain_stock = remain_stock - 1"));
		if (!reserved) return "库存不足";

		if (costPoints > 0) {
			boolean deducted = pointsAccountService.update(Wrappers.<PointsAccountEntity>lambdaUpdate()
				.eq(PointsAccountEntity::getId, account.getId())
				.ge(PointsAccountEntity::getAvailablePoints, costPoints)
				.setSql("available_points = available_points - " + costPoints)
				.setSql("total_spent_points = IFNULL(total_spent_points,0) + " + costPoints)
				.setSql("version = IFNULL(version,0) + 1"));
			if (!deducted) throw new ServiceException("绿豆不足或账户状态已变化");
			PointsAccountEntity updated = pointsAccountService.getById(account.getId());
			PointsLedgerEntity ledger = new PointsLedgerEntity();
			ledger.setUserId(userId);
			ledger.setChangeType("SPEND");
			ledger.setChangePoints(-costPoints);
			ledger.setBeforePoints((updated == null || updated.getAvailablePoints() == null ? 0 : updated.getAvailablePoints()) + costPoints);
			ledger.setAfterPoints(updated == null || updated.getAvailablePoints() == null ? 0 : updated.getAvailablePoints());
			ledger.setRuleCode("COUPON_POINTS_EXCHANGE");
			ledger.setBizType("COUPON_EXCHANGE");
			ledger.setBizId(String.valueOf(templateId));
			ledger.setRequestId(effectiveRequestId);
			ledger.setRemark("兑换优惠券：" + template.getCouponName());
			pointsLedgerService.save(ledger);
		}

		UserCouponEntity userCoupon = new UserCouponEntity();
		userCoupon.setUserId(userId);
		userCoupon.setCouponTemplateId(templateId);
		userCoupon.setCouponNo(buildCouponNo());
		userCoupon.setCouponStatus("UNUSED");
		userCoupon.setStatus(1);
		userCoupon.setRemainDurationMinutes(template.getDurationMinutes());
		userCoupon.setRemainTimes(template.getTotalTimes());
		userCoupon.setValidStartAt(validity.startAt());
		userCoupon.setValidEndAt(validity.endAt());
		userCouponService.save(userCoupon);

		CouponReceiveLogEntity log = new CouponReceiveLogEntity();
		log.setRequestId(effectiveRequestId);
		log.setUserId(userId);
		log.setCouponTemplateId(templateId);
		log.setReceiveChannel("APP");
		log.setStatus(1);
		try {
			couponReceiveLogService.save(log);
		} catch (DuplicateKeyException exception) {
			throw new ServiceException("领取请求已处理，请勿重复提交");
		}
		return "领取成功";
	}

	private String eligibilityError(CouponTemplateEntity template, Long userId) {
		if (template == null || Func.equals(template.getIsDeleted(), 1)) return "券模板不存在";
		if (!Func.equals(template.getStatus(), 1)) return "券模板不可领取";
		Date now = new Date();
		if (template.getReceiveStartAt() != null && now.before(template.getReceiveStartAt())) return "领取尚未开始";
		if (template.getReceiveEndAt() != null && now.after(template.getReceiveEndAt())) return "领取已结束";
		if (template.getRemainStock() == null || template.getRemainStock() <= 0) return "库存不足";

		String validityError = validityError(template, now);
		if (validityError != null) return validityError;

		PointsAccountEntity account = pointsAccountService.getOne(Wrappers.<PointsAccountEntity>lambdaQuery()
			.eq(PointsAccountEntity::getUserId, userId)
			.eq(PointsAccountEntity::getIsDeleted, 0)
			.last("limit 1"));
		int growthLevel = account == null || account.getGrowthLevel() == null ? 0 : account.getGrowthLevel();
		if (template.getMinGrowthLevel() != null && template.getMinGrowthLevel() > 0 && growthLevel < template.getMinGrowthLevel()) {
			return "成长等级不足";
		}
		if (Func.equals(template.getAuthRequired(), 1)) {
			User user = userService.getById(userId);
			if (user == null || !Func.equals(user.getAuthStatus(), 2)) return "需要完成认证后领取";
		}
		return null;
	}

	private String validityError(CouponTemplateEntity template, Date now) {
		String validType = normalizeValidType(template.getValidType());
		if (VALID_TYPE_FIXED.equals(validType)) {
			if (template.getValidEndAt() == null) return "券模板固定有效期未配置结束时间";
			if (template.getValidStartAt() != null && !template.getValidEndAt().after(template.getValidStartAt())) {
				return "券模板固定有效期开始时间必须早于结束时间";
			}
			if (!template.getValidEndAt().after(now)) return "优惠券已过有效期";
			return null;
		}
		if (VALID_TYPE_RELATIVE.equals(validType)) {
			if (template.getValidDays() == null || template.getValidDays() <= 0) return "券模板领取后有效天数未配置";
			return null;
		}
		return "券模板有效期类型配置错误";
	}

	private ValidityWindow resolveValidity(CouponTemplateEntity template, Date now) {
		String validType = normalizeValidType(template.getValidType());
		if (VALID_TYPE_FIXED.equals(validType)) {
			Date startAt = template.getValidStartAt() == null || template.getValidStartAt().before(now) ? now : template.getValidStartAt();
			return new ValidityWindow(startAt, template.getValidEndAt());
		}
		if (VALID_TYPE_RELATIVE.equals(validType)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(now);
			calendar.add(Calendar.DAY_OF_MONTH, template.getValidDays());
			return new ValidityWindow(now, calendar.getTime());
		}
		throw new ServiceException("券模板有效期类型配置错误");
	}

	private String normalizeValidType(String value) {
		return Func.toStr(value, "").trim().toUpperCase(Locale.ROOT);
	}

	private long countReceived(Long userId, Long templateId) {
		return userCouponService.count(Wrappers.<UserCouponEntity>lambdaQuery()
			.eq(UserCouponEntity::getUserId, userId)
			.eq(UserCouponEntity::getCouponTemplateId, templateId)
			.eq(UserCouponEntity::getIsDeleted, 0));
	}

	private String buildCouponNo() {
		return "CP" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
	}

	private record ValidityWindow(Date startAt, Date endAt) {
	}
}
