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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.modules.usercoupon.pojo.entity.UserCouponEntity;
import org.springblade.modules.usercoupon.pojo.vo.UserCouponVO;
import org.springblade.modules.usercoupon.excel.UserCouponExcel;
import org.springblade.modules.usercoupon.mapper.UserCouponMapper;
import org.springblade.modules.usercoupon.service.IUserCouponService;
import org.springblade.modules.couponverifylog.pojo.entity.CouponVerifyLogEntity;
import org.springblade.modules.couponverifylog.service.ICouponVerifyLogService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Date;

/**
 * 用户认证类型表 服务实现类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl extends BaseServiceImpl<UserCouponMapper, UserCouponEntity> implements IUserCouponService {

	private final ICouponVerifyLogService couponVerifyLogService;

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
	@Transactional(rollbackFor = Exception.class)
	public String useCouponById(Long userCouponId, String orderNo, Long merchantUserId, boolean fullUse, int consumeDurationMinutes, int consumeTimes) {
		UserCouponEntity coupon = getById(userCouponId);
		if (coupon == null || coupon.getIsDeleted() != null && coupon.getIsDeleted() == 1) return "券不存在";
		if (!"UNUSED".equalsIgnoreCase(coupon.getCouponStatus()) && !"PARTIAL_USED".equalsIgnoreCase(coupon.getCouponStatus())) return "券状态不可核销";
		if (coupon.getValidEndAt() != null && coupon.getValidEndAt().before(new Date())) return "券已过期";
		int duration = Math.max(consumeDurationMinutes, 0);
		int times = Math.max(consumeTimes, 0);
		boolean durationCoupon = coupon.getRemainDurationMinutes() != null && coupon.getRemainDurationMinutes() > 0;
		boolean timesCoupon = coupon.getRemainTimes() != null && coupon.getRemainTimes() > 0;
		if (!fullUse && !durationCoupon && !timesCoupon) return "该券不支持部分核销";
		if (durationCoupon) {
			int used = fullUse ? coupon.getRemainDurationMinutes() : duration;
			if (used <= 0 || used > coupon.getRemainDurationMinutes()) return "核销时长不合法";
			coupon.setRemainDurationMinutes(coupon.getRemainDurationMinutes() - used);
			duration = used;
		}
		if (timesCoupon) {
			int used = fullUse ? coupon.getRemainTimes() : times;
			if (used <= 0 || used > coupon.getRemainTimes()) return "核销次数不合法";
			coupon.setRemainTimes(coupon.getRemainTimes() - used);
			times = used;
		}
		boolean finished = (!durationCoupon || coupon.getRemainDurationMinutes() == 0) && (!timesCoupon || coupon.getRemainTimes() == 0);
		coupon.setCouponStatus(finished ? "USED" : "PARTIAL_USED");
		coupon.setUsedAt(new Date());
		coupon.setUsedOrderNo(orderNo);
		coupon.setVerifyMerchantUserId(merchantUserId);
		coupon.setVerifyAt(new Date());
		updateById(coupon);
		CouponVerifyLogEntity log = new CouponVerifyLogEntity();
		log.setUserCouponId(coupon.getId()); log.setUserId(coupon.getUserId()); log.setMerchantUserId(merchantUserId);
		log.setTemplateId(coupon.getCouponTemplateId()); log.setCouponNo(coupon.getCouponNo());
		log.setVerifyChannel("MINI_PROGRAM"); log.setVerifyResult(1); log.setVerifyStatus("FINISHED"); log.setOrderNo(orderNo);
		log.setExtJson("{\"consumeDurationMinutes\":" + duration + ",\"consumeTimes\":" + times + "}");
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

}

