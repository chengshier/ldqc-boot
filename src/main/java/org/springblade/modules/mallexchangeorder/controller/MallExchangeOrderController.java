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
package org.springblade.modules.mallexchangeorder.controller;

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
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.mallexchangeorder.pojo.entity.MallExchangeOrderEntity;
import org.springblade.modules.mallexchangeorder.pojo.vo.MallExchangeOrderVO;
import org.springblade.modules.mallexchangeorder.excel.MallExchangeOrderExcel;
import org.springblade.modules.mallexchangeorder.wrapper.MallExchangeOrderWrapper;
import org.springblade.modules.mallexchangeorder.service.IMallExchangeOrderService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 商城兑换订单 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-mallexchangeorder/mallExchangeOrder")
@Tag(name = "商城兑换订单", description = "商城兑换订单接口")
public class MallExchangeOrderController extends BladeController {

	private final IMallExchangeOrderService mallExchangeOrderService;

	/**
	 * 商城兑换订单 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入mallExchangeOrder")
	public R<MallExchangeOrderVO> detail(MallExchangeOrderEntity mallExchangeOrder) {
		MallExchangeOrderEntity detail = mallExchangeOrderService.getOne(Condition.getQueryWrapper(mallExchangeOrder));
		return R.data(MallExchangeOrderWrapper.build().entityVO(detail));
	}
	/**
	 * 商城兑换订单 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入mallExchangeOrder")
	public R<IPage<MallExchangeOrderVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> mallExchangeOrder, Query query) {
		IPage<MallExchangeOrderEntity> pages = mallExchangeOrderService.page(Condition.getPage(query), Condition.getQueryWrapper(mallExchangeOrder, MallExchangeOrderEntity.class));
		return R.data(MallExchangeOrderWrapper.build().pageVO(pages));
	}

	/**
	 * 商城兑换订单 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入mallExchangeOrder")
	public R<IPage<MallExchangeOrderVO>> page(MallExchangeOrderVO mallExchangeOrder, Query query) {
		IPage<MallExchangeOrderVO> pages = mallExchangeOrderService.selectMallExchangeOrderPage(Condition.getPage(query), mallExchangeOrder);
		return R.data(pages);
	}

	/**
	 * 商城兑换订单 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入mallExchangeOrder")
	public R save(@Valid @RequestBody MallExchangeOrderEntity mallExchangeOrder) {
		return R.status(mallExchangeOrderService.save(mallExchangeOrder));
	}

	/**
	 * 商城兑换订单 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入mallExchangeOrder")
	public R update(@Valid @RequestBody MallExchangeOrderEntity mallExchangeOrder) {
		return R.status(mallExchangeOrderService.updateById(mallExchangeOrder));
	}

	/**
	 * 商城兑换订单 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入mallExchangeOrder")
	public R submit(@Valid @RequestBody MallExchangeOrderEntity mallExchangeOrder) {
		return R.status(mallExchangeOrderService.saveOrUpdate(mallExchangeOrder));
	}

	/**
	 * 商城兑换订单 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(mallExchangeOrderService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-mallExchangeOrder")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入mallExchangeOrder")
	public void exportMallExchangeOrder(@Parameter(hidden = true) @RequestParam Map<String, Object> mallExchangeOrder, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<MallExchangeOrderEntity> queryWrapper = Condition.getQueryWrapper(mallExchangeOrder, MallExchangeOrderEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(MallExchangeOrder::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(MallExchangeOrderEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<MallExchangeOrderExcel> list = mallExchangeOrderService.exportMallExchangeOrder(queryWrapper);
		ExcelUtil.export(response, "商城兑换订单数据" + DateUtil.time(), "商城兑换订单数据表", list, MallExchangeOrderExcel.class);
	}

}


