package org.springblade.modules.venueapply.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.venue.pojo.entity.VenueEntity;
import org.springblade.modules.venue.service.IVenueService;
import org.springblade.modules.venueapply.pojo.entity.VenueApplyEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** 场馆入驻、审核和运营者场馆维护工作流。 */
@Service
@RequiredArgsConstructor
public class VenueOnboardingService {

	private static final String PENDING = "PENDING";
	private static final String APPROVED = "APPROVED";
	private static final String REJECTED = "REJECTED";
	private static final String CANCELLED = "CANCELLED";

	private final IVenueApplyService applyService;
	private final IVenueService venueService;

	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> submit(Map<String, Object> body, Long userId) {
		requireLogin(userId);
		String activeKey = userId + ":VENUE_APPLY";
		VenueApplyEntity active = applyService.getOne(Wrappers.<VenueApplyEntity>lambdaQuery()
			.eq(VenueApplyEntity::getActiveUniqueKey, activeKey)
			.eq(VenueApplyEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (active != null) throw new ServiceException("你已有待审核的场馆入驻申请");

		String applicantName = clean(body.get("applicantName"), 50);
		String applicantPhone = clean(body.get("applicantPhone"), 30);
		String merchantName = clean(body.get("merchantName"), 150);
		String venueName = clean(body.get("venueName"), 150);
		String address = clean(body.get("address"), 500);
		String licenseNo = clean(body.get("licenseNo"), 100);
		String licenseImage = clean(body.get("licenseImage"), 1000);
		if (Func.isBlank(applicantName)) throw new ServiceException("请填写联系人姓名");
		if (!applicantPhone.matches("^1\\d{10}$")) throw new ServiceException("请填写正确的联系人手机号");
		if (Func.isBlank(merchantName)) throw new ServiceException("请填写场馆经营主体名称");
		if (Func.isBlank(venueName)) throw new ServiceException("请填写场馆名称");
		if (Func.isBlank(address)) throw new ServiceException("请填写场馆详细地址");
		if (Func.isBlank(licenseNo) || Func.isBlank(licenseImage)) throw new ServiceException("请填写营业执照信息并上传证照");

		VenueApplyEntity apply = new VenueApplyEntity();
		apply.setRequestNo("VA" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT));
		apply.setActiveUniqueKey(activeKey);
		apply.setApplicantUserId(userId);
		apply.setApplicantName(applicantName);
		apply.setApplicantPhone(applicantPhone);
		apply.setMerchantName(merchantName);
		apply.setLicenseNo(licenseNo);
		apply.setLicenseImage(licenseImage);
		apply.setVenueName(venueName);
		apply.setVenueTypeId(Func.toLong(body.get("venueTypeId")));
		apply.setCoverImage(clean(body.get("coverImage"), 1000));
		apply.setImages(clean(body.get("images"), 4000));
		apply.setAddress(address);
		apply.setLongitude(toCoordinate(body.get("longitude"), "经度"));
		apply.setLatitude(toCoordinate(body.get("latitude"), "纬度"));
		apply.setBusinessHours(clean(body.get("businessHours"), 200));
		apply.setVenuePhone(clean(body.get("venuePhone"), 50));
		apply.setTags(clean(body.get("tags"), 300));
		apply.setDescription(clean(body.get("description"), 2000));
		apply.setServiceNotice(clean(body.get("serviceNotice"), 1000));
		apply.setApplyStatus(PENDING);
		apply.setSubmittedAt(new Date());
		try {
			applyService.save(apply);
		} catch (DuplicateKeyException exception) {
			throw new ServiceException("申请已提交，请勿重复操作");
		}
		return userView(apply);
	}

	public IPage<Map<String, Object>> myPage(long page, long limit, Long userId) {
		requireLogin(userId);
		IPage<VenueApplyEntity> source = applyService.page(new Page<>(safePage(page), safeLimit(limit)),
			Wrappers.<VenueApplyEntity>lambdaQuery()
				.eq(VenueApplyEntity::getApplicantUserId, userId)
				.eq(VenueApplyEntity::getIsDeleted, 0)
				.orderByDesc(VenueApplyEntity::getSubmittedAt));
		List<Map<String, Object>> records = new ArrayList<>();
		for (VenueApplyEntity item : source.getRecords()) records.add(userView(item));
		return mapPage(source, records);
	}

	public Map<String, Object> myDetail(Long id, Long userId) {
		requireLogin(userId);
		VenueApplyEntity apply = applyService.getOne(Wrappers.<VenueApplyEntity>lambdaQuery()
			.eq(VenueApplyEntity::getId, id)
			.eq(VenueApplyEntity::getApplicantUserId, userId)
			.eq(VenueApplyEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (apply == null) throw new ServiceException("入驻申请不存在");
		return userView(apply);
	}

	@Transactional(rollbackFor = Exception.class)
	public void cancel(Long id, Long userId) {
		requireLogin(userId);
		boolean changed = applyService.update(Wrappers.<VenueApplyEntity>lambdaUpdate()
			.eq(VenueApplyEntity::getId, id)
			.eq(VenueApplyEntity::getApplicantUserId, userId)
			.eq(VenueApplyEntity::getApplyStatus, PENDING)
			.eq(VenueApplyEntity::getIsDeleted, 0)
			.set(VenueApplyEntity::getApplyStatus, CANCELLED)
			.set(VenueApplyEntity::getActiveUniqueKey, null));
		if (!changed) throw new ServiceException("仅待审核申请可以取消");
	}

	public IPage<VenueApplyEntity> adminPage(long page, long limit, String status, String keyword) {
		return applyService.page(new Page<>(safePage(page), safeLimit(limit)),
			Wrappers.<VenueApplyEntity>lambdaQuery()
				.eq(!Func.isBlank(status), VenueApplyEntity::getApplyStatus, status)
				.and(!Func.isBlank(keyword), wrapper -> wrapper.like(VenueApplyEntity::getVenueName, keyword)
					.or().like(VenueApplyEntity::getMerchantName, keyword)
					.or().like(VenueApplyEntity::getApplicantName, keyword)
					.or().like(VenueApplyEntity::getApplicantPhone, keyword))
				.eq(VenueApplyEntity::getIsDeleted, 0)
				.orderByAsc(VenueApplyEntity::getApplyStatus)
				.orderByDesc(VenueApplyEntity::getSubmittedAt));
	}

	public VenueApplyEntity adminDetail(Long id) {
		VenueApplyEntity apply = applyService.getById(id);
		if (apply == null || Func.equals(apply.getIsDeleted(), 1)) throw new ServiceException("入驻申请不存在");
		return apply;
	}

	@Transactional(rollbackFor = Exception.class)
	public VenueApplyEntity audit(Long id, String action, String reason, Long adminUserId) {
		VenueApplyEntity apply = adminDetail(id);
		if (!PENDING.equals(apply.getApplyStatus())) throw new ServiceException("该申请已处理");
		String normalizedAction = Func.toStr(action, "").trim().toUpperCase(Locale.ROOT);
		if (!"APPROVE".equals(normalizedAction) && !"REJECT".equals(normalizedAction)) throw new ServiceException("审核动作不正确");
		if ("REJECT".equals(normalizedAction) && Func.isBlank(reason)) throw new ServiceException("驳回必须填写原因");

		if ("APPROVE".equals(normalizedAction)) {
			VenueEntity venue = new VenueEntity();
			venue.setName(apply.getVenueName());
			venue.setCoverImage(apply.getCoverImage());
			venue.setImages(apply.getImages());
			venue.setAddress(apply.getAddress());
			venue.setLongitude(apply.getLongitude());
			venue.setLatitude(apply.getLatitude());
			venue.setRating(BigDecimal.ZERO);
			venue.setTags(apply.getTags());
			venue.setBusinessHours(apply.getBusinessHours());
			venue.setPhone(Func.isBlank(apply.getVenuePhone()) ? apply.getApplicantPhone() : apply.getVenuePhone());
			venue.setDescription(apply.getDescription());
			venue.setTypeId(apply.getVenueTypeId());
			venue.setSortOrder("0");
			venue.setOwnerUserId(apply.getApplicantUserId());
			venue.setSourceApplyId(apply.getId());
			venue.setMerchantName(apply.getMerchantName());
			venue.setServiceNotice(apply.getServiceNotice());
			venue.setStatus(1);
			venueService.save(venue);
			apply.setVenueId(venue.getId());
			apply.setApplyStatus(APPROVED);
			apply.setAuditReason(Func.isBlank(reason) ? "审核通过" : clean(reason, 500));
		} else {
			apply.setApplyStatus(REJECTED);
			apply.setAuditReason(clean(reason, 500));
		}
		apply.setActiveUniqueKey(null);
		apply.setAuditUserId(adminUserId);
		apply.setAuditTime(new Date());
		applyService.updateById(apply);
		return apply;
	}

	public IPage<VenueEntity> myVenues(long page, long limit, Long userId) {
		requireLogin(userId);
		return venueService.page(new Page<>(safePage(page), safeLimit(limit)),
			Wrappers.<VenueEntity>lambdaQuery()
				.eq(VenueEntity::getOwnerUserId, userId)
				.eq(VenueEntity::getIsDeleted, 0)
				.orderByDesc(VenueEntity::getUpdateTime));
	}

	@Transactional(rollbackFor = Exception.class)
	public VenueEntity updateMyVenue(Map<String, Object> body, Long userId) {
		requireLogin(userId);
		Long venueId = Func.toLong(body.get("id"));
		if (venueId == null) throw new ServiceException("缺少场馆ID");
		VenueEntity venue = venueService.getOne(Wrappers.<VenueEntity>lambdaQuery()
			.eq(VenueEntity::getId, venueId)
			.eq(VenueEntity::getOwnerUserId, userId)
			.eq(VenueEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (venue == null) throw new ServiceException("场馆不存在或无权维护");
		venue.setCoverImage(clean(body.get("coverImage"), 1000));
		venue.setImages(clean(body.get("images"), 4000));
		venue.setBusinessHours(clean(body.get("businessHours"), 200));
		venue.setPhone(clean(body.get("phone"), 50));
		venue.setTags(clean(body.get("tags"), 300));
		venue.setDescription(clean(body.get("description"), 2000));
		venue.setServiceNotice(clean(body.get("serviceNotice"), 1000));
		venueService.updateById(venue);
		return venue;
	}

	private Map<String, Object> userView(VenueApplyEntity apply) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", apply.getId());
		result.put("requestNo", apply.getRequestNo());
		result.put("venueName", apply.getVenueName());
		result.put("merchantName", apply.getMerchantName());
		result.put("coverImage", apply.getCoverImage());
		result.put("address", apply.getAddress());
		result.put("applicantName", apply.getApplicantName());
		result.put("applicantPhone", maskPhone(apply.getApplicantPhone()));
		result.put("applyStatus", apply.getApplyStatus());
		result.put("applyStatusText", statusText(apply.getApplyStatus()));
		result.put("auditReason", apply.getAuditReason());
		result.put("venueId", apply.getVenueId());
		result.put("submittedAt", apply.getSubmittedAt());
		result.put("auditTime", apply.getAuditTime());
		return result;
	}

	private String statusText(String status) {
		if (PENDING.equals(status)) return "待审核";
		if (APPROVED.equals(status)) return "已通过";
		if (REJECTED.equals(status)) return "已驳回";
		if (CANCELLED.equals(status)) return "已取消";
		return "状态未知";
	}

	private BigDecimal toCoordinate(Object value, String field) {
		if (value == null || Func.isBlank(String.valueOf(value))) return null;
		try {
			return new BigDecimal(String.valueOf(value));
		} catch (NumberFormatException exception) {
			throw new ServiceException(field + "格式不正确");
		}
	}

	private void requireLogin(Long userId) {
		if (userId == null || userId <= 0) throw new ServiceException("请先登录");
	}
	private long safePage(long page) { return Math.max(page, 1); }
	private long safeLimit(long limit) { return Math.min(Math.max(limit, 1), 50); }
	private String clean(Object value, int maxLength) {
		String text = value == null ? "" : String.valueOf(value).trim();
		return text.length() > maxLength ? text.substring(0, maxLength) : text;
	}
	private String maskPhone(String phone) {
		if (phone == null || phone.length() < 7) return phone;
		return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
	}
	private <T> IPage<Map<String, Object>> mapPage(IPage<T> source, List<Map<String, Object>> records) {
		Page<Map<String, Object>> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
		result.setRecords(records);
		return result;
	}
}
