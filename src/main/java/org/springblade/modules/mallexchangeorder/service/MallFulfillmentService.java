package org.springblade.modules.mallexchangeorder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.mallexchangeorder.pojo.entity.MallExchangeOrderEntity;
import org.springblade.modules.mallproduct.pojo.entity.MallProductEntity;
import org.springblade.modules.mallproduct.service.IMallProductService;
import org.springblade.modules.pointsaccount.pojo.entity.PointsAccountEntity;
import org.springblade.modules.pointsaccount.service.IPointsAccountService;
import org.springblade.modules.pointsledger.pojo.entity.PointsLedgerEntity;
import org.springblade.modules.pointsledger.service.IPointsLedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

/**
 * 积分商城订单履约与取消退款服务。
 */
@Service
@RequiredArgsConstructor
public class MallFulfillmentService {

	private final IMallExchangeOrderService orderService;
	private final IMallProductService productService;
	private final IPointsAccountService pointsAccountService;
	private final IPointsLedgerService pointsLedgerService;

	public IPage<MallExchangeOrderEntity> adminPage(Query query, Map<String, Object> filters) {
		String fulfillmentStatus = Func.toStr(filters.get("fulfillmentStatus"), "");
		String fulfillmentType = Func.toStr(filters.get("fulfillmentType"), "");
		String orderNo = Func.toStr(filters.get("orderNo"), "");
		String productName = Func.toStr(filters.get("productName"), "");
		return orderService.page(Condition.getPage(query), Wrappers.<MallExchangeOrderEntity>lambdaQuery()
			.eq(MallExchangeOrderEntity::getIsDeleted, 0)
			.eq(Func.isNotBlank(fulfillmentStatus), MallExchangeOrderEntity::getFulfillmentStatus, fulfillmentStatus)
			.eq(Func.isNotBlank(fulfillmentType), MallExchangeOrderEntity::getFulfillmentType, fulfillmentType)
			.like(Func.isNotBlank(orderNo), MallExchangeOrderEntity::getOrderNo, orderNo)
			.like(Func.isNotBlank(productName), MallExchangeOrderEntity::getProductNameSnapshot, productName)
			.orderByAsc(MallExchangeOrderEntity::getFulfillmentStatus)
			.orderByDesc(MallExchangeOrderEntity::getCreateTime));
	}

	@Transactional(rollbackFor = Exception.class)
	public MallExchangeOrderEntity ship(Long orderId, String company, String logisticsNo, String remark) {
		MallExchangeOrderEntity order = requireOrder(orderId);
		if (!"SHIP".equalsIgnoreCase(order.getFulfillmentType())) throw new ServiceException("该订单不是快递发货订单");
		if (!"PENDING".equalsIgnoreCase(order.getFulfillmentStatus())
			&& !"PROCESSING".equalsIgnoreCase(order.getFulfillmentStatus())) {
			throw new ServiceException("当前订单状态不能发货");
		}
		if (Func.isBlank(company) || Func.isBlank(logisticsNo)) throw new ServiceException("请填写物流公司和物流单号");
		boolean updated = orderService.update(Wrappers.<MallExchangeOrderEntity>lambdaUpdate()
			.eq(MallExchangeOrderEntity::getId, orderId)
			.in(MallExchangeOrderEntity::getFulfillmentStatus, "PENDING", "PROCESSING")
			.set(MallExchangeOrderEntity::getFulfillmentStatus, "SENT")
			.set(MallExchangeOrderEntity::getDeliveryStatus, "SENT")
			.set(MallExchangeOrderEntity::getLogisticsCompany, company.trim())
			.set(MallExchangeOrderEntity::getLogisticsNo, logisticsNo.trim())
			.set(MallExchangeOrderEntity::getFulfillmentRemark, Func.toStr(remark, "").trim()));
		if (!updated) throw new ServiceException("订单状态已变化，请刷新后重试");
		return orderService.getById(orderId);
	}

	@Transactional(rollbackFor = Exception.class)
	public MallExchangeOrderEntity readyPickup(Long orderId, String remark) {
		MallExchangeOrderEntity order = requireOrder(orderId);
		if (!"PICKUP".equalsIgnoreCase(order.getFulfillmentType())) throw new ServiceException("该订单不是到店领取订单");
		if (!"PENDING".equalsIgnoreCase(order.getFulfillmentStatus())
			&& !"PROCESSING".equalsIgnoreCase(order.getFulfillmentStatus())) {
			throw new ServiceException("当前订单状态不能设为待领取");
		}
		boolean updated = orderService.update(Wrappers.<MallExchangeOrderEntity>lambdaUpdate()
			.eq(MallExchangeOrderEntity::getId, orderId)
			.in(MallExchangeOrderEntity::getFulfillmentStatus, "PENDING", "PROCESSING")
			.set(MallExchangeOrderEntity::getFulfillmentStatus, "READY")
			.set(MallExchangeOrderEntity::getFulfillmentRemark, Func.toStr(remark, "").trim()));
		if (!updated) throw new ServiceException("订单状态已变化，请刷新后重试");
		return orderService.getById(orderId);
	}

	@Transactional(rollbackFor = Exception.class)
	public MallExchangeOrderEntity issueVirtual(Long orderId, String virtualContent, String remark) {
		MallExchangeOrderEntity order = requireOrder(orderId);
		if (!"VIRTUAL".equalsIgnoreCase(order.getFulfillmentType())) throw new ServiceException("该订单不是虚拟权益订单");
		if (!"PENDING".equalsIgnoreCase(order.getFulfillmentStatus())
			&& !"PROCESSING".equalsIgnoreCase(order.getFulfillmentStatus())) {
			throw new ServiceException("当前订单状态不能发放权益");
		}
		if (Func.isBlank(virtualContent)) throw new ServiceException("请填写虚拟权益内容或兑换码");
		boolean updated = orderService.update(Wrappers.<MallExchangeOrderEntity>lambdaUpdate()
			.eq(MallExchangeOrderEntity::getId, orderId)
			.in(MallExchangeOrderEntity::getFulfillmentStatus, "PENDING", "PROCESSING")
			.set(MallExchangeOrderEntity::getFulfillmentStatus, "READY")
			.set(MallExchangeOrderEntity::getVirtualContent, virtualContent.trim())
			.set(MallExchangeOrderEntity::getFulfillmentRemark, Func.toStr(remark, "").trim()));
		if (!updated) throw new ServiceException("订单状态已变化，请刷新后重试");
		return orderService.getById(orderId);
	}

	@Transactional(rollbackFor = Exception.class)
	public MallExchangeOrderEntity complete(Long orderId, String pickupCode, String remark) {
		MallExchangeOrderEntity order = requireOrder(orderId);
		if (!"READY".equalsIgnoreCase(order.getFulfillmentStatus())) throw new ServiceException("订单尚未达到可完成状态");
		if ("PICKUP".equalsIgnoreCase(order.getFulfillmentType())
			&& !Func.toStr(order.getPickupCode(), "").equals(Func.toStr(pickupCode, "").trim())) {
			throw new ServiceException("领取码不正确");
		}
		boolean updated = orderService.update(Wrappers.<MallExchangeOrderEntity>lambdaUpdate()
			.eq(MallExchangeOrderEntity::getId, orderId)
			.eq(MallExchangeOrderEntity::getFulfillmentStatus, "READY")
			.set(MallExchangeOrderEntity::getFulfillmentStatus, "COMPLETED")
			.set(MallExchangeOrderEntity::getDeliveryStatus, "FINISHED")
			.set(MallExchangeOrderEntity::getOrderStatus, "COMPLETED")
			.set(MallExchangeOrderEntity::getCompletedAt, new Date())
			.set(MallExchangeOrderEntity::getFulfillmentRemark, Func.toStr(remark, order.getFulfillmentRemark())));
		if (!updated) throw new ServiceException("订单状态已变化，请刷新后重试");
		return orderService.getById(orderId);
	}

	@Transactional(rollbackFor = Exception.class)
	public MallExchangeOrderEntity cancelAndRefund(Long orderId, String reason) {
		if (Func.isBlank(reason)) throw new ServiceException("取消订单必须填写原因");
		MallExchangeOrderEntity order = requireOrder(orderId);
		if ("CANCELLED".equalsIgnoreCase(order.getOrderStatus())) return order;
		if ("COMPLETED".equalsIgnoreCase(order.getFulfillmentStatus()) || "SENT".equalsIgnoreCase(order.getFulfillmentStatus())) {
			throw new ServiceException("已完成或已发货订单不能直接取消，请走售后流程");
		}

		boolean orderUpdated = orderService.update(Wrappers.<MallExchangeOrderEntity>lambdaUpdate()
			.eq(MallExchangeOrderEntity::getId, orderId)
			.notIn(MallExchangeOrderEntity::getFulfillmentStatus, "COMPLETED", "SENT", "CANCELLED")
			.set(MallExchangeOrderEntity::getFulfillmentStatus, "CANCELLED")
			.set(MallExchangeOrderEntity::getOrderStatus, "CANCELLED")
			.set(MallExchangeOrderEntity::getCancelledAt, new Date())
			.set(MallExchangeOrderEntity::getCancelReason, reason.trim()));
		if (!orderUpdated) throw new ServiceException("订单状态已变化，请刷新后重试");

		int qty = Func.toInt(order.getQty(), 0);
		if (qty > 0) {
			productService.update(Wrappers.<MallProductEntity>lambdaUpdate()
				.eq(MallProductEntity::getId, order.getProductId())
				.setSql("stock_available = COALESCE(stock_available, 0) + " + qty)
				.setSql("sold_qty = GREATEST(COALESCE(sold_qty, 0) - " + qty + ", 0)"));
		}

		int refundPoints = Func.toInt(order.getSpendPoints(), 0);
		PointsAccountEntity account = pointsAccountService.getOne(Wrappers.<PointsAccountEntity>lambdaQuery()
			.eq(PointsAccountEntity::getUserId, order.getUserId())
			.eq(PointsAccountEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (account == null) throw new ServiceException("用户绿豆账户不存在，无法自动退款");
		int before = Func.toInt(account.getAvailablePoints(), 0);
		pointsAccountService.update(Wrappers.<PointsAccountEntity>lambdaUpdate()
			.eq(PointsAccountEntity::getId, account.getId())
			.setSql("available_points = available_points + " + refundPoints)
			.setSql("total_spent_points = GREATEST(COALESCE(total_spent_points, 0) - " + refundPoints + ", 0)"));

		PointsLedgerEntity ledger = new PointsLedgerEntity();
		ledger.setUserId(order.getUserId());
		ledger.setChangeType("CREDIT");
		ledger.setChangePoints(refundPoints);
		ledger.setBeforePoints(before);
		ledger.setAfterPoints(before + refundPoints);
		ledger.setRuleCode("MALL_EXCHANGE_REFUND");
		ledger.setBizType("MALL_ORDER");
		ledger.setBizId(order.getOrderNo());
		ledger.setRemark("商城兑换取消退款：" + order.getProductNameSnapshot());
		ledger.setRequestId("mall:refund:" + order.getOrderNo());
		ledger.setStatus(1);
		pointsLedgerService.save(ledger);
		return orderService.getById(orderId);
	}

	private MallExchangeOrderEntity requireOrder(Long orderId) {
		if (orderId == null) throw new ServiceException("缺少订单ID");
		MallExchangeOrderEntity order = orderService.getById(orderId);
		if (order == null || Func.equals(order.getIsDeleted(), 1)) throw new ServiceException("兑换订单不存在");
		return order;
	}
}
