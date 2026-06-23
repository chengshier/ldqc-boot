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
package org.springblade.modules.coupontemplate.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import lombok.AllArgsConstructor;
import jakarta.validation.Valid;

import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.secure.utils.AuthUtil;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.coupontemplate.pojo.entity.CouponTemplateEntity;
import org.springblade.modules.coupontemplate.pojo.vo.CouponTemplateVO;
import org.springblade.modules.coupontemplate.excel.CouponTemplateExcel;
import org.springblade.modules.coupontemplate.wrapper.CouponTemplateWrapper;
import org.springblade.modules.coupontemplate.service.ICouponTemplateService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 优惠券模板 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-coupontemplate/couponTemplate")
@Tag(name = "优惠券模板", description = "优惠券模板接口")
public class CouponTemplateController extends BladeController {

	private final ICouponTemplateService couponTemplateService;

	/**
	 * 优惠券模板 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入couponTemplate")
	public R<CouponTemplateVO> detail(CouponTemplateEntity couponTemplate) {
		CouponTemplateEntity detail = couponTemplateService.getOne(Condition.getQueryWrapper(couponTemplate));
		return R.data(CouponTemplateWrapper.build().entityVO(detail));
	}
	/**
	 * 优惠券模板 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入couponTemplate")
	public R<IPage<CouponTemplateVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> couponTemplate, Query query) {
		IPage<CouponTemplateEntity> pages = couponTemplateService.page(Condition.getPage(query), Condition.getQueryWrapper(couponTemplate, CouponTemplateEntity.class));
		return R.data(CouponTemplateWrapper.build().pageVO(pages));
	}

	/**
	 * 优惠券模板 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入couponTemplate")
	public R<IPage<CouponTemplateVO>> page(CouponTemplateVO couponTemplate, Query query) {
		IPage<CouponTemplateVO> pages = couponTemplateService.selectCouponTemplatePage(Condition.getPage(query), couponTemplate);
		return R.data(pages);
	}

	/**
	 * 优惠券模板 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入couponTemplate")
	public R save(@Valid @RequestBody CouponTemplateEntity couponTemplate) {
		return R.status(couponTemplateService.save(couponTemplate));
	}

	/**
	 * 优惠券模板 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入couponTemplate")
	public R update(@Valid @RequestBody CouponTemplateEntity couponTemplate) {
		return R.status(couponTemplateService.updateById(couponTemplate));
	}

	/**
	 * 优惠券模板 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入couponTemplate")
	public R submit(@Valid @RequestBody CouponTemplateEntity couponTemplate) {
		return R.status(couponTemplateService.saveOrUpdate(couponTemplate));
	}

	/**
	 * 优惠券模板 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(couponTemplateService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-couponTemplate")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入couponTemplate")
	public void exportCouponTemplate(@Parameter(hidden = true) @RequestParam Map<String, Object> couponTemplate, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<CouponTemplateEntity> queryWrapper = Condition.getQueryWrapper(couponTemplate, CouponTemplateEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(CouponTemplate::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(CouponTemplateEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<CouponTemplateExcel> list = couponTemplateService.exportCouponTemplate(queryWrapper);
		ExcelUtil.export(response, "优惠券模板数据" + DateUtil.time(), "优惠券模板数据表", list, CouponTemplateExcel.class);
	}


	@GetMapping("/receive-check")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "领券资格检查", description  = "传入templateId、growthLevel、authStatus")
	public R<String> receiveCheck(@RequestParam Long templateId,
	                              @RequestParam(required = false, defaultValue = "0") Integer growthLevel,
	                              @RequestParam(required = false, defaultValue = "0") Integer authStatus) {
		String result = couponTemplateService.receiveCheck(templateId, growthLevel, authStatus);
		return "可领取".equals(result) ? R.data(result) : R.fail(result);
	}

	@PostMapping("/receive")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "领取优惠券", description  = "传入templateId")
	public R<String> receive(@RequestParam Long templateId, @RequestParam(required = false) String requestId) {
		Long userId = AuthUtil.getUserId();
		String result = couponTemplateService.receive(templateId, requestId, userId);
		return "领取成功".equals(result) ? R.data(result) : R.fail(result);
	}
}


