package org.springblade.modules.coupontemplate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.coupontemplate.excel.CouponTemplateExcel;
import org.springblade.modules.coupontemplate.pojo.entity.CouponTemplateEntity;
import org.springblade.modules.coupontemplate.pojo.vo.CouponTemplateVO;
import org.springblade.modules.coupontemplate.service.ICouponTemplateService;
import org.springblade.modules.coupontemplate.wrapper.CouponTemplateWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** 优惠券模板公开领取和管理端维护接口。 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-coupontemplate/couponTemplate")
@Tag(name = "优惠券模板", description = "可领取优惠券、领取资格和管理端模板维护")
public class CouponTemplateController extends BladeController {

	private final ICouponTemplateService couponTemplateService;

	@GetMapping("/mobile/page")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "可领取优惠券列表")
	public R<IPage<CouponTemplateVO>> mobilePage(Query query) {
		Date now = new Date();
		IPage<CouponTemplateEntity> page = couponTemplateService.page(Condition.getPage(query),
			Wrappers.<CouponTemplateEntity>lambdaQuery()
				.eq(CouponTemplateEntity::getStatus, 1)
				.eq(CouponTemplateEntity::getIsDeleted, 0)
				.gt(CouponTemplateEntity::getRemainStock, 0)
				.and(item -> item.isNull(CouponTemplateEntity::getReceiveStartAt).or().le(CouponTemplateEntity::getReceiveStartAt, now))
				.and(item -> item.isNull(CouponTemplateEntity::getReceiveEndAt).or().ge(CouponTemplateEntity::getReceiveEndAt, now))
				.orderByAsc(CouponTemplateEntity::getCostPoints)
				.orderByDesc(CouponTemplateEntity::getCreateTime));
		return R.data(CouponTemplateWrapper.build().pageVO(page));
	}

	@GetMapping("/mobile/detail")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "可领取优惠券详情")
	public R<CouponTemplateVO> mobileDetail(@RequestParam Long id) {
		CouponTemplateEntity detail = couponTemplateService.getOne(Wrappers.<CouponTemplateEntity>lambdaQuery()
			.eq(CouponTemplateEntity::getId, id)
			.eq(CouponTemplateEntity::getStatus, 1)
			.eq(CouponTemplateEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (detail == null) return R.fail("优惠券不存在或已停用");
		return R.data(CouponTemplateWrapper.build().entityVO(detail));
	}

	@GetMapping("/receive-check")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "领券资格检查", description = "成长等级与认证状态以后端登录用户为准")
	public R<String> receiveCheck(@RequestParam Long templateId) {
		String result = couponTemplateService.receiveCheck(templateId, AuthUtil.getUserId());
		return "可领取".equals(result) ? R.data(result) : R.fail(result);
	}

	@PostMapping("/receive")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "领取优惠券", description = "requestId 必填，用于防止重复领取请求")
	public R<String> receive(@RequestParam Long templateId, @RequestParam String requestId) {
		String result = couponTemplateService.receive(templateId, requestId, AuthUtil.getUserId());
		return "领取成功".equals(result) ? R.data(result) : R.fail(result);
	}

	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "管理端模板详情")
	public R<CouponTemplateVO> detail(CouponTemplateEntity couponTemplate) {
		CouponTemplateEntity detail = couponTemplateService.getOne(Condition.getQueryWrapper(couponTemplate));
		return R.data(CouponTemplateWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "管理端模板分页")
	public R<IPage<CouponTemplateVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> couponTemplate, Query query) {
		IPage<CouponTemplateEntity> pages = couponTemplateService.page(
			Condition.getPage(query), Condition.getQueryWrapper(couponTemplate, CouponTemplateEntity.class));
		return R.data(CouponTemplateWrapper.build().pageVO(pages));
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "管理端模板关联分页")
	public R<IPage<CouponTemplateVO>> page(CouponTemplateVO couponTemplate, Query query) {
		return R.data(couponTemplateService.selectCouponTemplatePage(Condition.getPage(query), couponTemplate));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "新增优惠券模板")
	public R save(@Valid @RequestBody CouponTemplateEntity couponTemplate) {
		normalize(couponTemplate);
		return R.status(couponTemplateService.save(couponTemplate));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 14)
	@Operation(summary = "修改优惠券模板")
	public R update(@Valid @RequestBody CouponTemplateEntity couponTemplate) {
		normalize(couponTemplate);
		return R.status(couponTemplateService.updateById(couponTemplate));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 15)
	@Operation(summary = "保存优惠券模板")
	public R submit(@Valid @RequestBody CouponTemplateEntity couponTemplate) {
		normalize(couponTemplate);
		return R.status(couponTemplateService.saveOrUpdate(couponTemplate));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 16)
	@Operation(summary = "逻辑删除优惠券模板")
	public R remove(@RequestParam String ids) {
		return R.status(couponTemplateService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-couponTemplate")
	@ApiOperationSupport(order = 17)
	@Operation(summary = "导出优惠券模板")
	public void exportCouponTemplate(@Parameter(hidden = true) @RequestParam Map<String, Object> couponTemplate,
		BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<CouponTemplateEntity> queryWrapper = Condition.getQueryWrapper(couponTemplate, CouponTemplateEntity.class);
		List<CouponTemplateExcel> list = couponTemplateService.exportCouponTemplate(queryWrapper);
		ExcelUtil.export(response, "优惠券模板" + DateUtil.time(), "优惠券模板", list, CouponTemplateExcel.class);
	}

	private void normalize(CouponTemplateEntity template) {
		if (template.getRemainStock() == null && template.getTotalStock() != null) template.setRemainStock(template.getTotalStock());
		if (template.getPerUserLimit() == null || template.getPerUserLimit() <= 0) template.setPerUserLimit(1);
		if (template.getMinGrowthLevel() == null || template.getMinGrowthLevel() < 0) template.setMinGrowthLevel(0);
		template.setAuthRequired(Func.equals(template.getAuthRequired(), 1) ? 1 : 0);
		if ("POINTS_EXCHANGE".equalsIgnoreCase(template.getAcquireType())) {
			if (template.getCostPoints() == null || template.getCostPoints() <= 0) {
				throw new org.springblade.core.log.exception.ServiceException("绿豆兑换券必须配置大于0的兑换绿豆数");
			}
		} else {
			template.setAcquireType("FREE");
			template.setCostPoints(0);
		}
		if (template.getReceiveStartAt() != null && template.getReceiveEndAt() != null
			&& !template.getReceiveEndAt().after(template.getReceiveStartAt())) {
			throw new org.springblade.core.log.exception.ServiceException("领取结束时间必须晚于开始时间");
		}
	}
}
