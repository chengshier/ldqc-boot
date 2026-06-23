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
package org.springblade.modules.mallproduct.controller;

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
import org.springblade.modules.mallproduct.pojo.entity.MallProductEntity;
import org.springblade.modules.mallproduct.pojo.vo.MallProductVO;
import org.springblade.modules.mallproduct.excel.MallProductExcel;
import org.springblade.modules.mallproduct.wrapper.MallProductWrapper;
import org.springblade.modules.mallproduct.service.IMallProductService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 商城商品 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-mallproduct/mallProduct")
@Tag(name = "商城商品", description = "商城商品接口")
public class MallProductController extends BladeController {

	private final IMallProductService mallProductService;

	/**
	 * 商城商品 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入mallProduct")
	public R<MallProductVO> detail(MallProductEntity mallProduct) {
		MallProductEntity detail = mallProductService.getOne(Condition.getQueryWrapper(mallProduct));
		return R.data(MallProductWrapper.build().entityVO(detail));
	}
	/**
	 * 商城商品 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入mallProduct")
	public R<IPage<MallProductVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> mallProduct, Query query) {
		IPage<MallProductEntity> pages = mallProductService.page(Condition.getPage(query), Condition.getQueryWrapper(mallProduct, MallProductEntity.class));
		return R.data(MallProductWrapper.build().pageVO(pages));
	}

	/**
	 * 商城商品 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入mallProduct")
	public R<IPage<MallProductVO>> page(MallProductVO mallProduct, Query query) {
		IPage<MallProductVO> pages = mallProductService.selectMallProductPage(Condition.getPage(query), mallProduct);
		return R.data(pages);
	}

	/**
	 * 商城商品 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入mallProduct")
	public R save(@Valid @RequestBody MallProductEntity mallProduct) {
		return R.status(mallProductService.save(mallProduct));
	}

	/**
	 * 商城商品 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入mallProduct")
	public R update(@Valid @RequestBody MallProductEntity mallProduct) {
		return R.status(mallProductService.updateById(mallProduct));
	}

	/**
	 * 商城商品 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入mallProduct")
	public R submit(@Valid @RequestBody MallProductEntity mallProduct) {
		return R.status(mallProductService.saveOrUpdate(mallProduct));
	}

	/**
	 * 商城商品 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(mallProductService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-mallProduct")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入mallProduct")
	public void exportMallProduct(@Parameter(hidden = true) @RequestParam Map<String, Object> mallProduct, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<MallProductEntity> queryWrapper = Condition.getQueryWrapper(mallProduct, MallProductEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(MallProduct::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(MallProductEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<MallProductExcel> list = mallProductService.exportMallProduct(queryWrapper);
		ExcelUtil.export(response, "商城商品数据" + DateUtil.time(), "商城商品数据表", list, MallProductExcel.class);
	}

	@PostMapping("/exchange")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "积分兑换商品", description  = "传入productId、qty、requestId")
	public R<String> exchange(@RequestParam Long productId,
	                          @RequestParam(defaultValue = "1") Integer qty,
	                          @RequestParam(required = false) String requestId) {
		Long userId = AuthUtil.getUserId();
		String result = mallProductService.exchange(productId, qty, requestId, userId);
		if (Func.equals(result, "请先登录") || Func.equals(result, "数量非法") || Func.equals(result, "商品不存在")
			|| Func.equals(result, "商品未上架") || Func.equals(result, "库存不足") || Func.equals(result, "积分账户不存在")
			|| Func.equals(result, "绿豆不足")) {
			return R.fail(result);
		}
		return R.data(result);
	}

}


