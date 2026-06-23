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
package org.springblade.modules.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.modules.mallproduct.pojo.entity.MallProductEntity;
import org.springblade.modules.mallproduct.pojo.vo.MallProductVO;
import org.springblade.modules.mallproduct.excel.MallProductExcel;
import org.springblade.modules.mallproduct.mapper.MallProductMapper;
import org.springblade.modules.mallproduct.service.IMallProductService;
import org.springblade.modules.mallexchangeorder.pojo.entity.MallExchangeOrderEntity;
import org.springblade.modules.mallexchangeorder.service.IMallExchangeOrderService;
import org.springblade.modules.pointsaccount.pojo.entity.PointsAccountEntity;
import org.springblade.modules.pointsaccount.service.IPointsAccountService;
import org.springblade.modules.pointsledger.pojo.entity.PointsLedgerEntity;
import org.springblade.modules.pointsledger.service.IPointsLedgerService;
import org.springblade.core.tool.utils.Func;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;
import java.util.UUID;

/**
 * 用户认证类型表 服务实现类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
public class MallProductServiceImpl extends BaseServiceImpl<MallProductMapper, MallProductEntity> implements IMallProductService {

	private final IPointsAccountService pointsAccountService;
	private final IPointsLedgerService pointsLedgerService;
	private final IMallExchangeOrderService mallExchangeOrderService;

	@Override
	public IPage<MallProductVO> selectMallProductPage(IPage<MallProductVO> page, MallProductVO mallProduct) {
		return page.setRecords(baseMapper.selectMallProductPage(page, mallProduct));
	}


	@Override
	public List<MallProductExcel> exportMallProduct(Wrapper<MallProductEntity> queryWrapper) {
		List<MallProductExcel> mallProductList = baseMapper.exportMallProduct(queryWrapper);
		//mallProductList.forEach(mallProduct -> {
		//	mallProduct.setTypeName(DictCache.getValue(DictEnum.YES_NO, MallProduct.getType()));
		//});
		return mallProductList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String exchange(Long productId, Integer qty, String requestId, Long userId) {
		if (userId == null) return "请先登录";
		if (qty == null || qty <= 0) return "数量非法";
		String finalRequestId = Func.isBlank(requestId) ? UUID.randomUUID().toString() : requestId;
		MallExchangeOrderEntity existed = mallExchangeOrderService.getOne(Wrappers.<MallExchangeOrderEntity>lambdaQuery()
			.eq(MallExchangeOrderEntity::getRequestId, finalRequestId)
			.eq(MallExchangeOrderEntity::getIsDeleted, 0));
		if (existed != null) return existed.getOrderNo();

		MallProductEntity product = this.getById(productId);
		if (product == null || Func.equals(product.getIsDeleted(), 1)) return "商品不存在";
		if (!Func.equals(product.getStatus(), 1)) return "商品未上架";
		if (product.getStockAvailable() == null || product.getStockAvailable() < qty) return "库存不足";

		PointsAccountEntity account = pointsAccountService.getOne(Wrappers.<PointsAccountEntity>lambdaQuery()
			.eq(PointsAccountEntity::getUserId, userId)
			.eq(PointsAccountEntity::getIsDeleted, 0));
		if (account == null) return "积分账户不存在";
		int needPoints = (product.getSalePoints() == null ? 0 : product.getSalePoints()) * qty;
		if (account.getAvailablePoints() == null || account.getAvailablePoints() < needPoints) return "绿豆不足";

		boolean lockStockOk = this.update(Wrappers.<MallProductEntity>lambdaUpdate()
			.eq(MallProductEntity::getId, productId)
			.ge(MallProductEntity::getStockAvailable, qty)
			.setSql("stock_available = stock_available - " + qty));
		if (!lockStockOk) return "库存不足";

		boolean deductPointsOk = pointsAccountService.update(Wrappers.<PointsAccountEntity>lambdaUpdate()
			.eq(PointsAccountEntity::getId, account.getId())
			.ge(PointsAccountEntity::getAvailablePoints, needPoints)
			.setSql("available_points = available_points - " + needPoints)
			.setSql("total_spent_points = IFNULL(total_spent_points,0) + " + needPoints));
		if (!deductPointsOk) throw new IllegalStateException("绿豆不足或账户并发冲突");

		PointsAccountEntity latestAccount = pointsAccountService.getById(account.getId());

		MallExchangeOrderEntity order = new MallExchangeOrderEntity();
		order.setOrderNo("MO" + System.currentTimeMillis());
		order.setRequestId(finalRequestId);
		order.setUserId(userId);
		order.setProductId(productId);
		order.setQty(qty);
		order.setSpendPoints(needPoints);
		order.setOrderStatus("SUCCESS");
		order.setDeliveryStatus("PENDING");
		mallExchangeOrderService.save(order);

		PointsLedgerEntity ledger = new PointsLedgerEntity();
		ledger.setUserId(userId);
		ledger.setChangeType("SPEND");
		ledger.setChangePoints(-needPoints);
		ledger.setBeforePoints((latestAccount == null ? 0 : latestAccount.getAvailablePoints()) + needPoints);
		ledger.setAfterPoints(latestAccount == null ? 0 : latestAccount.getAvailablePoints());
		ledger.setBizType("MALL_EXCHANGE");
		ledger.setBizId(order.getOrderNo());
		ledger.setRemark("商城兑换扣减");
		ledger.setRequestId(order.getRequestId());
		pointsLedgerService.save(ledger);
		return order.getOrderNo();
	}

}

