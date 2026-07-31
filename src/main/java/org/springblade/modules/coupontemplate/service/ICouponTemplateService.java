package org.springblade.modules.coupontemplate.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.coupontemplate.excel.CouponTemplateExcel;
import org.springblade.modules.coupontemplate.pojo.entity.CouponTemplateEntity;
import org.springblade.modules.coupontemplate.pojo.vo.CouponTemplateVO;

import java.util.List;

public interface ICouponTemplateService extends BaseService<CouponTemplateEntity> {

	IPage<CouponTemplateVO> selectCouponTemplatePage(IPage<CouponTemplateVO> page, CouponTemplateVO couponTemplate);

	List<CouponTemplateExcel> exportCouponTemplate(Wrapper<CouponTemplateEntity> queryWrapper);

	/** 领券资格检查，成长等级和认证状态均由服务端查询。 */
	String receiveCheck(Long templateId, Long userId);

	/** 领取优惠券，requestId 必填并作为用户级幂等键。 */
	String receive(Long templateId, String requestId, Long userId);
}
