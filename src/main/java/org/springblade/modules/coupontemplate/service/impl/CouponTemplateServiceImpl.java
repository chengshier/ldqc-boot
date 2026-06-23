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
package org.springblade.modules.coupontemplate.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.modules.coupontemplate.pojo.entity.CouponTemplateEntity;
import org.springblade.modules.coupontemplate.pojo.vo.CouponTemplateVO;
import org.springblade.modules.coupontemplate.excel.CouponTemplateExcel;
import org.springblade.modules.coupontemplate.mapper.CouponTemplateMapper;
import org.springblade.modules.coupontemplate.service.ICouponTemplateService;
import org.springblade.modules.couponreceivelog.pojo.entity.CouponReceiveLogEntity;
import org.springblade.modules.couponreceivelog.service.ICouponReceiveLogService;
import org.springblade.modules.pointsaccount.pojo.entity.PointsAccountEntity;
import org.springblade.modules.pointsaccount.service.IPointsAccountService;
import org.springblade.modules.usercoupon.pojo.entity.UserCouponEntity;
import org.springblade.modules.usercoupon.service.IUserCouponService;
import org.springblade.core.tool.utils.Func;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;
import java.util.Date;
import java.util.UUID;
import java.util.Calendar;

/**
 * 用户认证类型表 服务实现类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
public class CouponTemplateServiceImpl extends BaseServiceImpl<CouponTemplateMapper, CouponTemplateEntity> implements ICouponTemplateService {

	private final IUserCouponService userCouponService;
	private final ICouponReceiveLogService couponReceiveLogService;
	private final IPointsAccountService pointsAccountService;

	@Override
	public IPage<CouponTemplateVO> selectCouponTemplatePage(IPage<CouponTemplateVO> page, CouponTemplateVO couponTemplate) {
		return page.setRecords(baseMapper.selectCouponTemplatePage(page, couponTemplate));
	}


	@Override
	public List<CouponTemplateExcel> exportCouponTemplate(Wrapper<CouponTemplateEntity> queryWrapper) {
		List<CouponTemplateExcel> couponTemplateList = baseMapper.exportCouponTemplate(queryWrapper);
		//couponTemplateList.forEach(couponTemplate -> {
		//	couponTemplate.setTypeName(DictCache.getValue(DictEnum.YES_NO, CouponTemplate.getType()));
		//});
		return couponTemplateList;
	}

	@Override
	public String receiveCheck(Long templateId, Integer growthLevel, Integer authStatus) {
		CouponTemplateEntity template = this.getById(templateId);
		if (template == null || Func.equals(template.getIsDeleted(), 1)) return "券模板不存在";
		if (template.getStatus() == null || template.getStatus() != 1) return "券模板不可领取";
		if (template.getRemainStock() != null && template.getRemainStock() <= 0) return "库存不足";
		int level = growthLevel == null ? 0 : growthLevel;
		if (template.getMinGrowthLevel() != null && level < template.getMinGrowthLevel()) return "成长等级不足";
		if (template.getExtJson() != null && template.getExtJson().contains("\"receive_auth_required\":true") && (authStatus == null || authStatus != 2)) {
			return "需要完成认证后领取";
		}
		return "可领取";
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String receive(Long templateId, String requestId, Long userId) {
		if (userId == null) return "请先登录";
		CouponTemplateEntity template = this.getById(templateId);
		if (template == null || Func.equals(template.getIsDeleted(), 1)) return "券模板不存在";
		if (!Func.equals(template.getStatus(), 1)) return "券模板不可领取";
		if (template.getRemainStock() == null || template.getRemainStock() <= 0) return "库存不足";

		PointsAccountEntity account = pointsAccountService.getOne(Wrappers.<PointsAccountEntity>lambdaQuery().eq(PointsAccountEntity::getUserId, userId));
		int growthLevel = account == null || account.getGrowthLevel() == null ? 0 : account.getGrowthLevel();
		if (template.getMinGrowthLevel() != null && growthLevel < template.getMinGrowthLevel()) return "成长等级不足";

		long receiveCount = userCouponService.count(Wrappers.<UserCouponEntity>lambdaQuery()
			.eq(UserCouponEntity::getUserId, userId)
			.eq(UserCouponEntity::getCouponTemplateId, templateId)
			.eq(UserCouponEntity::getIsDeleted, 0));
		if (template.getPerUserLimit() != null && receiveCount >= template.getPerUserLimit()) return "超过每人限领次数";

		Date now = new Date();
		Date endAt = template.getValidEndAt();
		if (Func.equals(template.getValidType(), "RELATIVE") && template.getValidDays() != null && template.getValidDays() > 0) {
			Calendar c = Calendar.getInstance();
			c.setTime(now);
			c.add(Calendar.DAY_OF_MONTH, template.getValidDays());
			endAt = c.getTime();
		}
		if (endAt == null) {
			Calendar c = Calendar.getInstance();
			c.setTime(now);
			c.add(Calendar.DAY_OF_MONTH, 30);
			endAt = c.getTime();
		}

		UserCouponEntity userCoupon = new UserCouponEntity();
		userCoupon.setUserId(userId);
		userCoupon.setCouponTemplateId(templateId);
		userCoupon.setCouponNo("CP" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6));
		userCoupon.setCouponStatus("UNUSED");
		userCoupon.setRemainDurationMinutes(template.getDurationMinutes());
		userCoupon.setRemainTimes(template.getTotalTimes());
		userCoupon.setValidStartAt(now);
		userCoupon.setValidEndAt(endAt);
		userCouponService.save(userCoupon);

		this.update(Wrappers.<CouponTemplateEntity>lambdaUpdate()
			.eq(CouponTemplateEntity::getId, templateId)
			.gt(CouponTemplateEntity::getRemainStock, 0)
			.setSql("remain_stock = remain_stock - 1"));

		CouponReceiveLogEntity log = new CouponReceiveLogEntity();
		log.setRequestId(Func.isBlank(requestId) ? UUID.randomUUID().toString() : requestId);
		log.setUserId(userId);
		log.setCouponTemplateId(templateId);
		log.setReceiveChannel("APP");
		log.setStatus(1);
		couponReceiveLogService.save(log);
		return "领取成功";
	}

}

