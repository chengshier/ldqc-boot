package org.springblade.modules.mallproduct.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.mallexchangeorder.pojo.entity.MallExchangeOrderEntity;
import org.springblade.modules.mallexchangeorder.service.MallFulfillmentService;
import org.springblade.modules.mallproduct.pojo.entity.MallProductEntity;
import org.springblade.modules.mallproduct.service.IMallProductService;
import org.springblade.modules.mallproduct.service.MallExchangeWorkflowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 积分商城用户兑换与运营履约接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("blade-mall/exchange")
@Tag(name = "积分商城兑换", description = "商品浏览、兑换确认、订单查询和运营履约")
public class MallExchangeController {

	private final IMallProductService productService;
	private final MallExchangeWorkflowService exchangeWorkflowService;
	private final MallFulfillmentService fulfillmentService;

	@GetMapping("/product-page")
	@Operation(summary = "商城商品列表", description = "只返回已上架且非优惠券商品")
	public R<IPage<MallProductEntity>> productPage(Query query,
														@RequestParam(required = false) String keyword,
														@RequestParam(required = false) String categoryCode,
														@RequestParam(required = false) String fulfillmentType,
														@RequestParam(required = false) Integer minPoints,
														@RequestParam(required = false) Integer maxPoints) {
		IPage<MallProductEntity> page = productService.page(Condition.getPage(query),
			Wrappers.<MallProductEntity>lambdaQuery()
				.eq(MallProductEntity::getStatus, 1)
				.eq(MallProductEntity::getIsDeleted, 0)
				.ne(MallProductEntity::getProductType, "COUPON")
				.and(Func.isNotBlank(keyword), wrapper -> wrapper
					.like(MallProductEntity::getProductName, keyword)
					.or().like(MallProductEntity::getProductDesc, keyword)
					.or().like(MallProductEntity::getMerchantName, keyword))
				.eq(Func.isNotBlank(categoryCode), MallProductEntity::getCategoryCode, categoryCode)
				.eq(Func.isNotBlank(fulfillmentType), MallProductEntity::getFulfillmentType, fulfillmentType)
				.ge(minPoints != null, MallProductEntity::getSalePoints, minPoints)
				.le(maxPoints != null, MallProductEntity::getSalePoints, maxPoints)
				.gt(MallProductEntity::getStockAvailable, 0)
				.orderByDesc(MallProductEntity::getSortNo)
				.orderByDesc(MallProductEntity::getPublishedAt)
				.orderByDesc(MallProductEntity::getCreateTime));
		return R.data(page);
	}

	@GetMapping("/product-detail")
	@Operation(summary = "商城商品详情")
	public R<Map<String, Object>> productDetail(@RequestParam Long id) {
		return R.data(exchangeWorkflowService.productDetail(id));
	}

	@GetMapping("/confirm")
	@Operation(summary = "兑换确认信息", description = "返回绿豆余额、兑换后余额、限兑和履约要求")
	public R<Map<String, Object>> confirm(@RequestParam Long productId,
											 @RequestParam(defaultValue = "1") Integer qty,
											 @RequestParam(required = false) String spec) {
		return R.data(exchangeWorkflowService.confirm(productId, qty, spec, AuthUtil.getUserId()));
	}

	@PostMapping("/submit")
	@Operation(summary = "确认兑换", description = "原子扣库存和绿豆并生成商品快照订单")
	public R<MallExchangeOrderEntity> submit(@RequestBody Map<String, Object> body) {
		return R.data(exchangeWorkflowService.exchange(body, AuthUtil.getUserId()));
	}

	@GetMapping("/my-orders")
	@Operation(summary = "我的兑换记录")
	public R<IPage<MallExchangeOrderEntity>> myOrders(Query query,
														@RequestParam(required = false) String fulfillmentStatus) {
		return R.data(exchangeWorkflowService.myOrders(query, fulfillmentStatus, AuthUtil.getUserId()));
	}

	@GetMapping("/my-order-detail")
	@Operation(summary = "我的兑换详情")
	public R<MallExchangeOrderEntity> myOrderDetail(@RequestParam Long id) {
		return R.data(exchangeWorkflowService.myOrderDetail(id, AuthUtil.getUserId()));
	}

	@PostMapping("/confirm-receipt")
	@Operation(summary = "确认收货")
	public R<MallExchangeOrderEntity> confirmReceipt(@RequestBody Map<String, Object> body) {
		Long orderId = Func.toLong(body.get("orderId"));
		if (orderId == null) return R.fail("缺少订单ID");
		return R.data(exchangeWorkflowService.confirmReceipt(orderId, AuthUtil.getUserId()));
	}

	@IsAdmin
	@GetMapping("/admin-page")
	@Operation(summary = "运营兑换订单列表")
	public R<IPage<MallExchangeOrderEntity>> adminPage(Query query, @RequestParam Map<String, Object> filters) {
		return R.data(fulfillmentService.adminPage(query, filters));
	}

	@IsAdmin
	@PostMapping("/admin-ship")
	@Operation(summary = "快递发货")
	public R<MallExchangeOrderEntity> ship(@RequestBody Map<String, Object> body) {
		return R.data(fulfillmentService.ship(
			Func.toLong(body.get("orderId")),
			Func.toStr(body.get("logisticsCompany"), ""),
			Func.toStr(body.get("logisticsNo"), ""),
			Func.toStr(body.get("remark"), "")));
	}

	@IsAdmin
	@PostMapping("/admin-ready-pickup")
	@Operation(summary = "设置为待领取")
	public R<MallExchangeOrderEntity> readyPickup(@RequestBody Map<String, Object> body) {
		return R.data(fulfillmentService.readyPickup(
			Func.toLong(body.get("orderId")), Func.toStr(body.get("remark"), "")));
	}

	@IsAdmin
	@PostMapping("/admin-issue-virtual")
	@Operation(summary = "发放虚拟权益")
	public R<MallExchangeOrderEntity> issueVirtual(@RequestBody Map<String, Object> body) {
		return R.data(fulfillmentService.issueVirtual(
			Func.toLong(body.get("orderId")),
			Func.toStr(body.get("virtualContent"), ""),
			Func.toStr(body.get("remark"), "")));
	}

	@IsAdmin
	@PostMapping("/admin-complete")
	@Operation(summary = "完成到店领取或虚拟权益订单")
	public R<MallExchangeOrderEntity> complete(@RequestBody Map<String, Object> body) {
		return R.data(fulfillmentService.complete(
			Func.toLong(body.get("orderId")),
			Func.toStr(body.get("pickupCode"), ""),
			Func.toStr(body.get("remark"), "")));
	}

	@IsAdmin
	@PostMapping("/admin-cancel")
	@Operation(summary = "取消订单并退还绿豆", description = "未发货、未完成订单可取消；库存和绿豆同事务恢复")
	public R<MallExchangeOrderEntity> cancel(@RequestBody Map<String, Object> body) {
		return R.data(fulfillmentService.cancelAndRefund(
			Func.toLong(body.get("orderId")), Func.toStr(body.get("reason"), "")));
	}
}
