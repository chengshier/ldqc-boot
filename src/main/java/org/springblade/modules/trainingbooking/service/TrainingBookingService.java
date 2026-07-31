package org.springblade.modules.trainingbooking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.training.service.ITrainingService;
import org.springblade.modules.trainingbooking.mapper.TrainingBookingMapper;
import org.springblade.modules.trainingbooking.pojo.entity.TrainingBookingEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** 体育课程线下预约业务。 */
@Service
@RequiredArgsConstructor
public class TrainingBookingService {

	private final TrainingBookingMapper bookingMapper;
	private final ITrainingService trainingService;
	private final IUserService userService;

	@Transactional(rollbackFor = Exception.class)
	public TrainingBookingEntity submit(Map<String, Object> body, Long userId) {
		User user = requireUser(userId);
		if (body == null) throw new ServiceException("预约参数不能为空");
		Long trainingId = Func.toLong(body.get("trainingId"));
		String requestId = clean(body.get("requestId"), 64);
		if (trainingId == null || trainingId <= 0) throw new ServiceException("缺少课程ID");
		if (Func.isBlank(requestId)) throw new ServiceException("缺少预约请求号");

		TrainingBookingEntity existed = bookingMapper.selectOne(Wrappers.<TrainingBookingEntity>lambdaQuery()
			.eq(TrainingBookingEntity::getUserId, userId)
			.eq(TrainingBookingEntity::getRequestId, requestId)
			.eq(TrainingBookingEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (existed != null) return existed;

		TrainingEntity training = trainingService.getOne(Wrappers.<TrainingEntity>lambdaQuery()
			.eq(TrainingEntity::getId, trainingId)
			.eq(TrainingEntity::getStatus, 1)
			.eq(TrainingEntity::getPublishStatus, "PUBLISHED")
			.eq(TrainingEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (training == null) throw new ServiceException("课程不存在或未发布");
		String contentMode = Func.toStr(training.getContentMode(), "OFFLINE").trim().toUpperCase(Locale.ROOT);
		if ("ONLINE".equals(contentMode)) throw new ServiceException("纯线上课程不支持线下预约");

		long activeCount = bookingMapper.selectCount(Wrappers.<TrainingBookingEntity>lambdaQuery()
			.eq(TrainingBookingEntity::getUserId, userId)
			.eq(TrainingBookingEntity::getTrainingId, trainingId)
			.in(TrainingBookingEntity::getBookingStatus, "SUBMITTED", "CONFIRMED")
			.eq(TrainingBookingEntity::getIsDeleted, 0));
		if (activeCount > 0) throw new ServiceException("你已有待处理或已确认的课程预约");

		String contactName = clean(body.get("contactName"), 100);
		String contactPhone = clean(body.get("contactPhone"), 32);
		if (Func.isBlank(contactName)) contactName = Func.toStr(user.getName(), "").trim();
		if (Func.isBlank(contactName)) throw new ServiceException("请填写联系人");
		if (!isPhone(contactPhone)) throw new ServiceException("请填写正确的联系电话");
		int participantCount = parsePositiveInt(body.get("participantCount"), 1, 20);

		TrainingBookingEntity booking = new TrainingBookingEntity();
		booking.setBookingNo(createBookingNo());
		booking.setRequestId(requestId);
		booking.setUserId(userId);
		booking.setTrainingId(training.getId());
		booking.setTrainingTitleSnapshot(training.getTitle());
		booking.setCoverImageSnapshot(training.getCoverImage());
		booking.setContentModeSnapshot(contentMode);
		booking.setCourseTypeSnapshot(training.getCourseType());
		booking.setPriceSnapshot(training.getPrice() == null ? BigDecimal.ZERO : training.getPrice());
		booking.setLocationSnapshot(training.getLocation());
		booking.setAddressSnapshot(training.getAddress());
		booking.setContactName(contactName);
		booking.setContactPhone(contactPhone);
		booking.setParticipantCount(participantCount);
		booking.setPreferredTime(clean(body.get("preferredTime"), 100));
		booking.setRemark(clean(body.get("remark"), 500));
		booking.setBookingStatus("SUBMITTED");
		booking.setStatus(1);
		bookingMapper.insert(booking);
		return booking;
	}

	public IPage<TrainingBookingEntity> myPage(long current, long size, String bookingStatus, Long userId) {
		requireUser(userId);
		String status = normalizeStatus(bookingStatus, false);
		return bookingMapper.selectPage(new Page<>(Math.max(current, 1), Math.min(Math.max(size, 1), 50)),
			Wrappers.<TrainingBookingEntity>lambdaQuery()
				.eq(TrainingBookingEntity::getUserId, userId)
				.eq(TrainingBookingEntity::getIsDeleted, 0)
				.eq(Func.isNotBlank(status), TrainingBookingEntity::getBookingStatus, status)
				.orderByDesc(TrainingBookingEntity::getCreateTime));
	}

	public TrainingBookingEntity myDetail(Long bookingId, Long userId) {
		requireUser(userId);
		if (bookingId == null || bookingId <= 0) throw new ServiceException("缺少预约ID");
		TrainingBookingEntity booking = bookingMapper.selectOne(Wrappers.<TrainingBookingEntity>lambdaQuery()
			.eq(TrainingBookingEntity::getId, bookingId)
			.eq(TrainingBookingEntity::getUserId, userId)
			.eq(TrainingBookingEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (booking == null) throw new ServiceException("预约记录不存在");
		return booking;
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingBookingEntity cancel(Long bookingId, String reason, Long userId) {
		TrainingBookingEntity booking = myDetail(bookingId, userId);
		String current = normalizeStatus(booking.getBookingStatus(), true);
		if (!"SUBMITTED".equals(current) && !"CONFIRMED".equals(current)) {
			throw new ServiceException("当前预约状态不能取消");
		}
		booking.setBookingStatus("CANCELLED");
		booking.setCancelledAt(new Date());
		booking.setAuditReason(Func.isBlank(reason) ? "用户主动取消" : clean(reason, 500));
		bookingMapper.updateById(booking);
		return booking;
	}

	public IPage<TrainingBookingEntity> adminPage(long current, long size, String bookingStatus,
		String keyword, Long trainingId) {
		String status = normalizeStatus(bookingStatus, false);
		String text = Func.toStr(keyword, "").trim();
		return bookingMapper.selectPage(new Page<>(Math.max(current, 1), Math.min(Math.max(size, 1), 100)),
			Wrappers.<TrainingBookingEntity>query()
				.eq("is_deleted", 0)
				.eq(Func.isNotBlank(status), "booking_status", status)
				.eq(trainingId != null && trainingId > 0, "training_id", trainingId)
				.and(Func.isNotBlank(text), item -> item
					.like("booking_no", text)
					.or().like("training_title_snapshot", text)
					.or().like("contact_name", text)
					.or().like("contact_phone", text))
				.orderByDesc("create_time"));
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingBookingEntity confirm(Long bookingId, String reason) {
		TrainingBookingEntity booking = requireBooking(bookingId);
		if (!"SUBMITTED".equals(normalizeStatus(booking.getBookingStatus(), true))) {
			throw new ServiceException("只有待确认预约可以确认");
		}
		booking.setBookingStatus("CONFIRMED");
		booking.setConfirmedAt(new Date());
		booking.setAuditReason(Func.isBlank(reason) ? "平台已确认预约，请按约定时间到场" : clean(reason, 500));
		bookingMapper.updateById(booking);
		return booking;
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingBookingEntity reject(Long bookingId, String reason) {
		TrainingBookingEntity booking = requireBooking(bookingId);
		if (!"SUBMITTED".equals(normalizeStatus(booking.getBookingStatus(), true))) {
			throw new ServiceException("只有待确认预约可以驳回");
		}
		if (Func.isBlank(reason)) throw new ServiceException("驳回时必须填写原因");
		booking.setBookingStatus("REJECTED");
		booking.setAuditReason(clean(reason, 500));
		bookingMapper.updateById(booking);
		return booking;
	}

	@Transactional(rollbackFor = Exception.class)
	public TrainingBookingEntity complete(Long bookingId, String reason) {
		TrainingBookingEntity booking = requireBooking(bookingId);
		if (!"CONFIRMED".equals(normalizeStatus(booking.getBookingStatus(), true))) {
			throw new ServiceException("只有已确认预约可以完成");
		}
		booking.setBookingStatus("COMPLETED");
		booking.setCompletedAt(new Date());
		if (Func.isNotBlank(reason)) booking.setAuditReason(clean(reason, 500));
		bookingMapper.updateById(booking);
		return booking;
	}

	private TrainingBookingEntity requireBooking(Long bookingId) {
		if (bookingId == null || bookingId <= 0) throw new ServiceException("缺少预约ID");
		TrainingBookingEntity booking = bookingMapper.selectOne(Wrappers.<TrainingBookingEntity>lambdaQuery()
			.eq(TrainingBookingEntity::getId, bookingId)
			.eq(TrainingBookingEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (booking == null) throw new ServiceException("预约记录不存在");
		return booking;
	}

	private User requireUser(Long userId) {
		if (userId == null || userId <= 0) throw new ServiceException("请先登录");
		User user = userService.getById(userId);
		if (user == null || Func.equals(user.getIsDeleted(), 1) || !Func.equals(user.getStatus(), 1)) {
			throw new ServiceException("用户不存在或已停用");
		}
		return user;
	}

	private String normalizeStatus(String value, boolean required) {
		if (Func.isBlank(value)) return required ? "SUBMITTED" : "";
		String status = value.trim().toUpperCase(Locale.ROOT);
		if (!"SUBMITTED".equals(status) && !"CONFIRMED".equals(status) && !"REJECTED".equals(status)
			&& !"CANCELLED".equals(status) && !"COMPLETED".equals(status)) {
			throw new ServiceException("预约状态不正确");
		}
		return status;
	}

	private String createBookingNo() {
		String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
		return "TB" + time + suffix;
	}

	private int parsePositiveInt(Object value, int defaultValue, int maxValue) {
		int parsed = Func.toInt(value, defaultValue);
		if (parsed <= 0 || parsed > maxValue) throw new ServiceException("参与人数应在1到" + maxValue + "之间");
		return parsed;
	}

	private boolean isPhone(String value) {
		if (Func.isBlank(value)) return false;
		return value.matches("^[0-9+()\\-\\s]{6,32}$");
	}

	private String clean(Object value, int maxLength) {
		String text = value == null ? "" : String.valueOf(value).trim();
		return text.length() > maxLength ? text.substring(0, maxLength) : text;
	}
}
