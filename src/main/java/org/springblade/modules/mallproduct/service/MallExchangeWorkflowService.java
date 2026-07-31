package org.springblade.modules.mallproduct.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.mallexchangeorder.pojo.entity.MallExchangeOrderEntity;
import org.springblade.modules.mallexchangeorder.service.IMallExchangeOrderService;
import org.springblade.modules.mallproduct.pojo.entity.MallProductEntity;
import org.springblade.modules.pointsaccount.pojo.entity.PointsAccountEntity;
import org.springblade.modules.pointsaccount.service.IPointsAccountService;
import org.springblade.modules.pointsledger.pojo.entity.PointsLedgerEntity;
import org.springblade.modules.pointsledger.service.IPointsLedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 积分商城确认、兑换和用户订单查询工作流。
 */
@Service
@RequiredArgsConstructor
public class MallExchangeWorkflowService {

	private final IMallProductService productService;
	private final IMallExchangeOrderService orderService;
	private final IPointsAccountService pointsAccountService;
	private final IPointsLedgerService pointsLedgerService;

	public Map<String, Object> productDetail(Long productId) {
		MallProductEntity product = requireAvailableProduct(productId);
		return buildProductDetail(product);
	}

	public Map<String, Object> confirm(Long productId, Integer qtyValue, String spec, Long userId) {
		if (userId == null || userId <= 0) throw new ServiceException("请先登录后再兑换");
		MallProductEntity product = requireAvailableProduct(productId);
		int qty = validateQuantity(product, qtyValue);
		String specSnapshot = validateSpec(product, spec);
		PointsAccountEntity account = pointsAccountService.getOne(Wrappers.<PointsAccountEntity>lambdaQuery()
			.eq(PointsAccountEntity::getUserId, userId)
			.eq(PointsAccountEntity::getIsDeleted, 0)
			.last("limit 1"));
		int balance = account == null ? 0 : Func.toInt(account.getAvailablePoints(), 0);
		int totalPoints = Math.multiplyExact(Func.toInt(product.getSalePoints(), 0), qty);
		int exchangedQty = exchangedQuantity(userId, productId);
		validatePerUserLimit(product, exchangedQty, qty);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("product", buildProductDetail(product));
		result.put("qty", qty);
		result.put("spec", specSnapshot);
		result.put("unitPoints", Func.toInt(product.getSalePoints(), 0));
		result.put("totalPoints", totalPoints);
		result.put("currentPoints", balance);
		result.put("afterPoints", balance - totalPoints);
		result.put("enoughPoints", balance >= totalPoints);
		result.put("exchangedQty", exchangedQty);
		result.put("remainingUserLimit", remainingLimit(product, exchangedQty));
		result.put("requireAddress", "SHIP".equals(normalizeFulfillment(product)) && !Func.equals(product.getRequireAddress(), 0));
		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	public MallExchangeOrderEntity exchange(Map<String, Object> body, Long userId) {
		if (userId == null || userId <= 0) throw new ServiceException("请先登录后再兑换");
		Long productId = Func.toLong(body.get("productId"));
		String requestId = Func.toStr(body.get("requestId"), "").trim();
		if (productId == null) throw new ServiceException("缺少商品ID");
		if (Func.isBlank(requestId)) throw new ServiceException("缺少兑换请求号，请刷新确认页后重试");
		if (requestId.length() > 128) throw new ServiceException("兑换请求号过长");

		MallExchangeOrderEntity existed = orderService.getOne(Wrappers.<MallExchangeOrderEntity>lambdaQuery()
			.eq(MallExchangeOrderEntity::getUserId, userId)
			.eq(MallExchangeOrderEntity::getRequestId, requestId)
			.eq(MallExchangeOrderEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (existed != null) return existed;

		MallProductEntity product = requireAvailableProduct(productId);
		int qty = validateQuantity(product, Func.toInt(body.get("qty"), 1));
		String specSnapshot = validateSpec(product, Func.toStr(body.get("spec"), ""));
		int exchangedQty = exchangedQuantity(userId, productId);
		validatePerUserLimit(product, exchangedQty, qty);
		int unitPoints = Func.toInt(product.getSalePoints(), 0);
		if (unitPoints <= 0) throw new ServiceException("商品兑换绿豆配置不正确");
		int spendPoints = Math.multiplyExact(unitPoints, qty);
		String fulfillmentType = normalizeFulfillment(product);
		validateReceiver(body, product, fulfillmentType);

		boolean stockUpdated = productService.update(Wrappers.<MallProductEntity>lambdaUpdate()
			.eq(MallProductEntity::getId, productId)
			.eq(MallProductEntity::getStatus, 1)
			.eq(MallProductEntity::getIsDeleted, 0)
			.ge(MallProductEntity::getStockAvailable, qty)
			.setSql("stock_available = stock_available - " + qty)
			.setSql("sold_qty = COALESCE(sold_qty, 0) + " + qty));
		if (!stockUpdated) throw new ServiceException("库存不足，请返回商品页刷新");

		PointsAccountEntity account = pointsAccountService.getOne(Wrappers.<PointsAccountEntity>lambdaQuery()
			.eq(PointsAccountEntity::getUserId, userId)
			.eq(PointsAccountEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (account == null) throw new ServiceException("绿豆账户不存在");
		int beforePoints = Func.toInt(account.getAvailablePoints(), 0);
		boolean pointsUpdated = pointsAccountService.update(Wrappers.<PointsAccountEntity>lambdaUpdate()
			.eq(PointsAccountEntity::getId, account.getId())
			.ge(PointsAccountEntity::getAvailablePoints, spendPoints)
			.setSql("available_points = available_points - " + spendPoints)
			.setSql("total_spent_points = COALESCE(total_spent_points, 0) + " + spendPoints));
		if (!pointsUpdated) throw new ServiceException("绿豆不足，请返回确认页刷新");

		MallExchangeOrderEntity order = buildOrder(body, product, userId, requestId, qty, unitPoints, spendPoints, specSnapshot, fulfillmentType);
		if (!orderService.save(order)) throw new ServiceException("兑换订单创建失败");

		PointsLedgerEntity ledger = new PointsLedgerEntity();
		ledger.setUserId(userId);
		ledger.setChangeType("DEBIT");
		ledger.setChangePoints(-spendPoints);
		ledger.setBeforePoints(beforePoints);
		ledger.setAfterPoints(beforePoints - spendPoints);
		ledger.setRuleCode("MALL_EXCHANGE");
		ledger.setBizType("MALL_ORDER");
		ledger.setBizId(order.getOrderNo());
		ledger.setRemark("积分商城兑换：" + product.getProductName() + " × " + qty);
		ledger.setRequestId("mall:debit:" + userId + ":" + requestId);
		ledger.setStatus(1);
		pointsLedgerService.save(ledger);
		return order;
	}

	public IPage<MallExchangeOrderEntity> myOrders(Query query, String fulfillmentStatus, Long userId) {
		if (userId == null || userId <= 0) throw new ServiceException("请先登录");
		return orderService.page(Condition.getPage(query), Wrappers.<MallExchangeOrderEntity>lambdaQuery()
			.eq(MallExchangeOrderEntity::getUserId, userId)
			.eq(MallExchangeOrderEntity::getIsDeleted, 0)
			.eq(Func.isNotBlank(fulfillmentStatus), MallExchangeOrderEntity::getFulfillmentStatus, fulfillmentStatus)
			.orderByDesc(MallExchangeOrderEntity::getCreateTime));
	}

	public MallExchangeOrderEntity myOrderDetail(Long orderId, Long userId) {
		MallExchangeOrderEntity order = orderService.getById(orderId);
		if (order == null || Func.equals(order.getIsDeleted(), 1) || !Objects.equals(order.getUserId(), userId)) {
			throw new ServiceException("兑换订单不存在或无权查看");
		}
		return order;
	}

	@Transactional(rollbackFor = Exception.class)
	public MallExchangeOrderEntity confirmReceipt(Long orderId, Long userId) {
		MallExchangeOrderEntity order = myOrderDetail(orderId, userId);
		if (!"SHIP".equalsIgnoreCase(order.getFulfillmentType()) || !"SENT".equalsIgnoreCase(order.getFulfillmentStatus())) {
			throw new ServiceException("当前订单不能确认收货");
		}
		boolean updated = orderService.update(Wrappers.<MallExchangeOrderEntity>lambdaUpdate()
			.eq(MallExchangeOrderEntity::getId, orderId)
			.eq(MallExchangeOrderEntity::getUserId, userId)
			.eq(MallExchangeOrderEntity::getFulfillmentStatus, "SENT")
			.set(MallExchangeOrderEntity::getFulfillmentStatus, "COMPLETED")
			.set(MallExchangeOrderEntity::getDeliveryStatus, "FINISHED")
			.set(MallExchangeOrderEntity::getOrderStatus, "COMPLETED")
			.set(MallExchangeOrderEntity::getCompletedAt, new Date()));
		if (!updated) throw new ServiceException("订单状态已变化，请刷新后重试");
		return orderService.getById(orderId);
	}

	private MallExchangeOrderEntity buildOrder(Map<String, Object> body,
													 MallProductEntity product,
													 Long userId,
													 String requestId,
													 int qty,
													 int unitPoints,
													 int spendPoints,
													 String specSnapshot,
													 String fulfillmentType) {
		MallExchangeOrderEntity order = new MallExchangeOrderEntity();
		order.setOrderNo("MX" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT));
		order.setRequestId(requestId);
		order.setUserId(userId);
		order.setProductId(product.getId());
		order.setQty(qty);
		order.setSpendPoints(spendPoints);
		order.setProductCodeSnapshot(product.getProductCode());
		order.setProductNameSnapshot(product.getProductName());
		order.setCoverUrlSnapshot(product.getCoverUrl());
		order.setSpecSnapshot(specSnapshot);
		order.setUnitPoints(unitPoints);
		order.setFulfillmentType(fulfillmentType);
		order.setMerchantNameSnapshot(product.getMerchantName());
		order.setReceiverName(trim(body.get("receiverName")));
		order.setReceiverPhone(trim(body.get("receiverPhone")));
		order.setReceiverAddress(trim(body.get("receiverAddress")));
		order.setPickupAddressSnapshot(product.getPickupAddress());
		if ("PICKUP".equals(fulfillmentType)) order.setPickupCode(generatePickupCode());
		order.setFulfillmentStatus("PENDING");
		order.setDeliveryStatus("NONE");
		order.setOrderStatus("SUCCESS");
		order.setStatus(1);
		return order;
	}

	private Map<String, Object> buildProductDetail(MallProductEntity product) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", product.getId());
		result.put("productCode", product.getProductCode());
		result.put("productName", product.getProductName());
		result.put("productDesc", product.getProductDesc());
		result.put("productType", product.getProductType());
		result.put("coverUrl", product.getCoverUrl());
		result.put("gallery", parseJsonList(product.getGalleryJson()));
		result.put("categoryCode", product.getCategoryCode());
		result.put("categoryName", product.getCategoryName());
		result.put("specs", parseJsonList(product.getSpecJson()));
		result.put("exchangeNotice", product.getExchangeNotice());
		result.put("salePoints", product.getSalePoints());
		result.put("marketAmount", product.getMarketAmount());
		result.put("stockAvailable", product.getStockAvailable());
		result.put("soldQty", product.getSoldQty());
		result.put("fulfillmentType", normalizeFulfillment(product));
		result.put("merchantName", product.getMerchantName());
		result.put("pickupAddress", product.getPickupAddress());
		result.put("perUserLimit", product.getPerUserLimit());
		result.put("maxQtyPerOrder", Math.max(1, Func.toInt(product.getMaxQtyPerOrder(), 1)));
		result.put("requireAddress", !Func.equals(product.getRequireAddress(), 0));
		return result;
	}

	private MallProductEntity requireAvailableProduct(Long productId) {
		MallProductEntity product = productService.getById(productId);
		if (product == null || Func.equals(product.getIsDeleted(), 1)) throw new ServiceException("商品不存在");
		if (!Func.equals(product.getStatus(), 1)) throw new ServiceException("商品已下架");
		if ("COUPON".equalsIgnoreCase(Func.toStr(product.getProductType(), ""))) {
			throw new ServiceException("优惠券请从优惠券入口领取，商城不直接兑换优惠券");
		}
		return product;
	}

	private int validateQuantity(MallProductEntity product, Integer qtyValue) {
		int qty = Func.toInt(qtyValue, 1);
		if (qty <= 0) throw new ServiceException("兑换数量必须大于0");
		int max = Math.max(1, Func.toInt(product.getMaxQtyPerOrder(), 1));
		if (qty > max) throw new ServiceException("单次最多兑换" + max + "件");
		if (Func.toInt(product.getStockAvailable(), 0) < qty) throw new ServiceException("库存不足");
		return qty;
	}

	private void validatePerUserLimit(MallProductEntity product, int exchangedQty, int currentQty) {
		int limit = Func.toInt(product.getPerUserLimit(), 0);
		if (limit > 0 && exchangedQty + currentQty > limit) {
			throw new ServiceException("该商品每人累计限兑" + limit + "件，你已兑换" + exchangedQty + "件");
		}
	}

	private int exchangedQuantity(Long userId, Long productId) {
		return orderService.list(Wrappers.<MallExchangeOrderEntity>lambdaQuery()
			.eq(MallExchangeOrderEntity::getUserId, userId)
			.eq(MallExchangeOrderEntity::getProductId, productId)
			.eq(MallExchangeOrderEntity::getIsDeleted, 0)
			.notIn(MallExchangeOrderEntity::getOrderStatus, "FAILED", "CANCELLED"))
			.stream().mapToInt(item -> Func.toInt(item.getQty(), 0)).sum();
	}

	private int remainingLimit(MallProductEntity product, int exchangedQty) {
		int limit = Func.toInt(product.getPerUserLimit(), 0);
		return limit <= 0 ? -1 : Math.max(0, limit - exchangedQty);
	}

	private String validateSpec(MallProductEntity product, String requestedSpec) {
		String spec = Func.toStr(requestedSpec, "").trim();
		if (spec.length() > 500) throw new ServiceException("商品规格内容过长");
		if (Func.isNotBlank(product.getSpecJson())) {
			if (Func.isBlank(spec)) throw new ServiceException("请选择商品规格");
			if (!product.getSpecJson().contains(spec)) throw new ServiceException("所选商品规格不存在或已下架");
		}
		return spec;
	}

	private void validateReceiver(Map<String, Object> body, MallProductEntity product, String fulfillmentType) {
		if ("SHIP".equals(fulfillmentType) && !Func.equals(product.getRequireAddress(), 0)) {
			String name = trim(body.get("receiverName"));
			String phone = trim(body.get("receiverPhone"));
			String address = trim(body.get("receiverAddress"));
			if (Func.isBlank(name) || Func.isBlank(phone) || Func.isBlank(address)) {
				throw new ServiceException("请填写完整收货人、手机号和收货地址");
			}
			if (!phone.matches("^1\\d{10}$")) throw new ServiceException("收货手机号格式不正确");
		}
		if ("PICKUP".equals(fulfillmentType) && Func.isBlank(product.getPickupAddress())) {
			throw new ServiceException("商品尚未配置领取地点");
		}
	}

	private String normalizeFulfillment(MallProductEntity product) {
		String value = Func.toStr(product.getFulfillmentType(), "SHIP").trim().toUpperCase(Locale.ROOT);
		return "PICKUP".equals(value) || "VIRTUAL".equals(value) ? value : "SHIP";
	}

	private List<Object> parseJsonList(String value) {
		if (Func.isBlank(value)) return Collections.emptyList();
		try {
			List<Object> list = JSON.parseArray(value, Object.class);
			return list == null ? Collections.emptyList() : list;
		} catch (Exception exception) {
			List<Object> fallback = new ArrayList<>();
			fallback.add(value);
			return fallback;
		}
	}

	private String generatePickupCode() {
		return String.valueOf(10000000 + Math.abs(UUID.randomUUID().hashCode() % 90000000));
	}

	private String trim(Object value) {
		return Func.toStr(value, "").trim();
	}
}
