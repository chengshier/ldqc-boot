package org.springblade.modules.competitionsignup.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.competition.pojo.entity.CompetitionEntity;
import org.springblade.modules.competition.service.ICompetitionService;
import org.springblade.modules.competitionsignup.pojo.entity.CompetitionSignupEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 赛事报名唯一业务入口。
 *
 * <p>负责服务端价格、报名窗口、重复报名、名额原子占用、免费报名、待支付订单、
 * 取消和超时释放。微信支付未接入时，付费赛事明确阻断，绝不由前端伪造已支付。</p>
 */
@Service
@RequiredArgsConstructor
public class CompetitionSignupWorkflowService {

	private static final String STATUS_PENDING_PAYMENT = "PENDING_PAYMENT";
	private static final String STATUS_CONFIRMED = "CONFIRMED";
	private static final String STATUS_CANCELLED = "CANCELLED";
	private static final String STATUS_EXPIRED = "EXPIRED";
	private static final String STATUS_LEGACY_REVIEW = "LEGACY_REVIEW";
	private static final int PAYMENT_EXPIRE_MINUTES = 15;

	private final ICompetitionService competitionService;
	private final ICompetitionSignupService signupService;

	@Value("${competition.payment.wechat-enabled:false}")
	private boolean wechatPaymentEnabled;

	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> create(Map<String, Object> body, Long userId) {
		requireLogin(userId);
		Long competitionId = Func.toLong(body.get("competitionId"));
		if (competitionId == null) throw new ServiceException("缺少赛事ID");
		String requestId = clean(body.get("requestId"), 64);
		if (Func.isBlank(requestId)) throw new ServiceException("缺少报名请求号，请刷新页面后重试");

		CompetitionSignupEntity idempotent = signupService.getOne(Wrappers.<CompetitionSignupEntity>lambdaQuery()
			.eq(CompetitionSignupEntity::getUserId, userId)
			.eq(CompetitionSignupEntity::getRequestId, requestId)
			.eq(CompetitionSignupEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (idempotent != null) return toUserView(idempotent);

		CompetitionEntity competition = competitionService.getById(competitionId);
		validateCompetition(competition);
		int people = Math.max(1, Func.toInt(body.get("numPeople"), 1));
		int maxPerOrder = competition.getMaxPeoplePerOrder() == null || competition.getMaxPeoplePerOrder() <= 0
			? 1 : competition.getMaxPeoplePerOrder();
		if (people > maxPerOrder) throw new ServiceException("单次最多可报名" + maxPerOrder + "人");

		String signupName = clean(body.get("signupName"), 50);
		String phone = clean(body.get("phone"), 30);
		String idCard = clean(body.get("idCard"), 40);
		if (Func.isBlank(signupName)) throw new ServiceException("请填写真实姓名");
		if (!phone.matches("^1\\d{10}$")) throw new ServiceException("请填写正确的手机号");
		if (!idCard.matches("^(\\d{15}|\\d{17}[0-9Xx])$")) throw new ServiceException("请填写正确的身份证号");

		BigDecimal unitPrice = safeMoney(competition.getPrice());
		String paymentMode = resolvePaymentMode(competition, unitPrice);
		if (unitPrice.compareTo(BigDecimal.ZERO) > 0 && !wechatPaymentEnabled) {
			throw new ServiceException("该赛事为付费赛事，微信支付通道尚未启用，暂不能报名");
		}

		String activeKey = competitionId + ":" + userId;
		CompetitionSignupEntity duplicate = signupService.getOne(Wrappers.<CompetitionSignupEntity>lambdaQuery()
			.eq(CompetitionSignupEntity::getActiveUniqueKey, activeKey)
			.eq(CompetitionSignupEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (duplicate != null) throw new ServiceException("你已报名该赛事，请勿重复提交");

		boolean seatReserved = competitionService.update(Wrappers.<CompetitionEntity>update()
			.eq("id", competitionId)
			.eq("is_deleted", 0)
			.eq("status", 1)
			.apply("(signup_start_time IS NULL OR signup_start_time <= NOW())")
			.apply("(signup_end_time IS NULL OR signup_end_time >= NOW())")
			.apply("(max_participants IS NULL OR max_participants <= 0 OR IFNULL(participant_count,0) + {0} <= max_participants)", people)
			.setSql("participant_count = IFNULL(participant_count,0) + " + people));
		if (!seatReserved) throw new ServiceException("报名已截止、赛事状态已变化或剩余名额不足");

		Date now = new Date();
		boolean free = unitPrice.compareTo(BigDecimal.ZERO) == 0;
		CompetitionSignupEntity order = new CompetitionSignupEntity();
		order.setOrderNo(buildOrderNo());
		order.setRequestId(requestId);
		order.setActiveUniqueKey(activeKey);
		order.setCompetitionId(competitionId);
		order.setUserId(userId);
		order.setCompetitionTitle(competition.getTitle());
		order.setCompetitionCover(competition.getCoverImage());
		order.setCompetitionStartTime(competition.getStartTime());
		order.setCompetitionEndTime(competition.getEndTime());
		order.setCompetitionLocation(competition.getLocation());
		order.setCompetitionAddress(competition.getAddress());
		order.setSignupName(signupName);
		order.setPhone(phone);
		order.setIdCard(idCard.toUpperCase(Locale.ROOT));
		order.setTeamName(clean(body.get("teamName"), 80));
		order.setNumPeople(people);
		order.setUnitPrice(unitPrice);
		order.setTotalAmount(unitPrice.multiply(BigDecimal.valueOf(people)).setScale(2, RoundingMode.HALF_UP));
		order.setPaymentMode(paymentMode);
		order.setPayStatus(free ? 1 : 0);
		order.setOrderStatus(free ? STATUS_CONFIRMED : STATUS_PENDING_PAYMENT);
		order.setPaidAt(free ? now : null);
		order.setPaymentExpireTime(free ? null : new Date(now.getTime() + PAYMENT_EXPIRE_MINUTES * 60_000L));
		order.setSignupTime(now);
		order.setRemark(clean(body.get("remark"), 500));

		try {
			signupService.save(order);
		} catch (DuplicateKeyException exception) {
			throw new ServiceException("报名请求已处理或你已报名该赛事，请勿重复提交");
		}
		return toUserView(order);
	}

	public IPage<Map<String, Object>> myPage(int current, int size, Long userId) {
		requireLogin(userId);
		IPage<CompetitionSignupEntity> source = signupService.page(
			new Page<>(Math.max(current, 1), Math.min(Math.max(size, 1), 50)),
			Wrappers.<CompetitionSignupEntity>lambdaQuery()
				.eq(CompetitionSignupEntity::getUserId, userId)
				.eq(CompetitionSignupEntity::getIsDeleted, 0)
				.orderByDesc(CompetitionSignupEntity::getSignupTime));
		Page<Map<String, Object>> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
		List<Map<String, Object>> records = new ArrayList<>();
		for (CompetitionSignupEntity item : source.getRecords()) records.add(toUserView(item));
		result.setRecords(records);
		return result;
	}

	public Map<String, Object> myDetail(Long id, Long userId) {
		requireLogin(userId);
		CompetitionSignupEntity order = signupService.getOne(Wrappers.<CompetitionSignupEntity>lambdaQuery()
			.eq(CompetitionSignupEntity::getId, id)
			.eq(CompetitionSignupEntity::getUserId, userId)
			.eq(CompetitionSignupEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (order == null) throw new ServiceException("报名订单不存在");
		return toUserView(order);
	}

	@Transactional(rollbackFor = Exception.class)
	public void cancel(Long id, String reason, Long userId) {
		requireLogin(userId);
		CompetitionSignupEntity order = signupService.getOne(Wrappers.<CompetitionSignupEntity>lambdaQuery()
			.eq(CompetitionSignupEntity::getId, id)
			.eq(CompetitionSignupEntity::getUserId, userId)
			.eq(CompetitionSignupEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (order == null) throw new ServiceException("报名订单不存在");
		if (STATUS_CANCELLED.equals(order.getOrderStatus()) || STATUS_EXPIRED.equals(order.getOrderStatus())) return;
		if (order.getPayStatus() != null && order.getPayStatus() == 1
			&& safeMoney(order.getTotalAmount()).compareTo(BigDecimal.ZERO) > 0) {
			throw new ServiceException("付费订单不能直接取消，请发起退款申请");
		}
		if (order.getCompetitionStartTime() != null && !new Date().before(order.getCompetitionStartTime())) {
			throw new ServiceException("赛事已开始，不能取消报名");
		}
		boolean changed = signupService.update(Wrappers.<CompetitionSignupEntity>lambdaUpdate()
			.eq(CompetitionSignupEntity::getId, id)
			.eq(CompetitionSignupEntity::getUserId, userId)
			.in(CompetitionSignupEntity::getOrderStatus, STATUS_PENDING_PAYMENT, STATUS_CONFIRMED)
			.set(CompetitionSignupEntity::getOrderStatus, STATUS_CANCELLED)
			.set(CompetitionSignupEntity::getActiveUniqueKey, null)
			.set(CompetitionSignupEntity::getCancelledAt, new Date())
			.set(CompetitionSignupEntity::getCancelReason, clean(reason, 500)));
		if (!changed) throw new ServiceException("订单状态已变化，请刷新后重试");
		releaseSeats(order);
	}

	/** 定时关闭超时未支付订单并释放名额。 */
	@Transactional(rollbackFor = Exception.class)
	public int expireUnpaidOrders() {
		List<CompetitionSignupEntity> expired = signupService.list(Wrappers.<CompetitionSignupEntity>lambdaQuery()
			.eq(CompetitionSignupEntity::getOrderStatus, STATUS_PENDING_PAYMENT)
			.lt(CompetitionSignupEntity::getPaymentExpireTime, new Date())
			.eq(CompetitionSignupEntity::getIsDeleted, 0)
			.last("limit 200"));
		int count = 0;
		for (CompetitionSignupEntity order : expired) {
			boolean changed = signupService.update(Wrappers.<CompetitionSignupEntity>lambdaUpdate()
				.eq(CompetitionSignupEntity::getId, order.getId())
				.eq(CompetitionSignupEntity::getOrderStatus, STATUS_PENDING_PAYMENT)
				.set(CompetitionSignupEntity::getOrderStatus, STATUS_EXPIRED)
				.set(CompetitionSignupEntity::getActiveUniqueKey, null)
				.set(CompetitionSignupEntity::getCancelledAt, new Date())
				.set(CompetitionSignupEntity::getCancelReason, "支付超时自动关闭"));
			if (changed) {
				releaseSeats(order);
				count++;
			}
		}
		return count;
	}

	private void releaseSeats(CompetitionSignupEntity order) {
		int people = Math.max(1, order.getNumPeople() == null ? 1 : order.getNumPeople());
		competitionService.update(Wrappers.<CompetitionEntity>update()
			.eq("id", order.getCompetitionId())
			.eq("is_deleted", 0)
			.setSql("participant_count = GREATEST(IFNULL(participant_count,0) - " + people + ", 0)"));
	}

	private void validateCompetition(CompetitionEntity competition) {
		if (competition == null || Func.equals(competition.getIsDeleted(), 1)) throw new ServiceException("赛事不存在");
		if (!Func.equals(competition.getStatus(), 1)) throw new ServiceException("赛事当前不可报名");
		Date now = new Date();
		if (competition.getSignupStartTime() != null && now.before(competition.getSignupStartTime())) {
			throw new ServiceException("报名尚未开始");
		}
		Date deadline = competition.getSignupEndTime() != null ? competition.getSignupEndTime() : competition.getStartTime();
		if (deadline != null && now.after(deadline)) throw new ServiceException("报名已截止");
		if (competition.getStartTime() != null && !now.before(competition.getStartTime())) throw new ServiceException("赛事已开始");
	}

	private String resolvePaymentMode(CompetitionEntity competition, BigDecimal price) {
		if (price.compareTo(BigDecimal.ZERO) == 0) return "FREE";
		String mode = clean(competition.getPaymentMode(), 16).toUpperCase(Locale.ROOT);
		if (Func.isBlank(mode)) mode = "WECHAT";
		if (!"WECHAT".equals(mode)) throw new ServiceException("该赛事支付方式暂不支持");
		return mode;
	}

	private Map<String, Object> toUserView(CompetitionSignupEntity order) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", order.getId());
		result.put("orderNo", order.getOrderNo());
		result.put("competitionId", order.getCompetitionId());
		result.put("competitionTitle", order.getCompetitionTitle());
		result.put("competitionCover", order.getCompetitionCover());
		result.put("competitionStartTime", order.getCompetitionStartTime());
		result.put("competitionEndTime", order.getCompetitionEndTime());
		result.put("competitionLocation", order.getCompetitionLocation());
		result.put("competitionAddress", order.getCompetitionAddress());
		result.put("signupName", order.getSignupName());
		result.put("phone", maskPhone(order.getPhone()));
		result.put("idCard", maskIdCard(order.getIdCard()));
		result.put("teamName", order.getTeamName());
		result.put("numPeople", order.getNumPeople());
		result.put("unitPrice", order.getUnitPrice());
		result.put("totalAmount", order.getTotalAmount());
		result.put("paymentMode", order.getPaymentMode());
		result.put("payStatus", order.getPayStatus());
		result.put("orderStatus", order.getOrderStatus());
		result.put("orderStatusText", statusText(order.getOrderStatus()));
		result.put("paymentExpireTime", order.getPaymentExpireTime());
		result.put("signupTime", order.getSignupTime());
		result.put("cancelReason", order.getCancelReason());
		result.put("paymentRequired", safeMoney(order.getTotalAmount()).compareTo(BigDecimal.ZERO) > 0);
		result.put("paymentAvailable", wechatPaymentEnabled);
		return result;
	}

	private String statusText(String status) {
		if (STATUS_PENDING_PAYMENT.equals(status)) return "待支付";
		if (STATUS_CONFIRMED.equals(status)) return "报名成功";
		if (STATUS_CANCELLED.equals(status)) return "已取消";
		if (STATUS_EXPIRED.equals(status)) return "支付超时";
		if ("REFUND_PENDING".equals(status)) return "退款处理中";
		if ("REFUNDED".equals(status)) return "已退款";
		if (STATUS_LEGACY_REVIEW.equals(status)) return "历史订单待核对";
		return "状态未知";
	}

	private void requireLogin(Long userId) {
		if (userId == null || userId <= 0) throw new ServiceException("请先登录");
	}

	private BigDecimal safeMoney(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
	}

	private String clean(Object value, int maxLength) {
		String text = value == null ? "" : String.valueOf(value).trim();
		return text.length() > maxLength ? text.substring(0, maxLength) : text;
	}

	private String buildOrderNo() {
		return "CS" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
	}

	private String maskPhone(String phone) {
		if (phone == null || phone.length() < 7) return phone;
		return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
	}

	private String maskIdCard(String idCard) {
		if (idCard == null || idCard.length() < 8) return "***";
		return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
	}
}
