package org.springblade.modules.mallproduct.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.mallproduct.pojo.entity.MallProductEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 面向运营人员的积分商城商品服务。
 */
@Service
@RequiredArgsConstructor
public class MallProductOperationService {

	private final IMallProductService productService;

	public IPage<MallProductEntity> page(Query query, Map<String, Object> filters) {
		String keyword = Func.toStr(filters.get("keyword"), "").trim();
		String fulfillmentType = Func.toStr(filters.get("fulfillmentType"), "").trim();
		String categoryCode = Func.toStr(filters.get("categoryCode"), "").trim();
		Integer status = filters.get("status") == null || Func.isBlank(String.valueOf(filters.get("status")))
			? null : Func.toInt(filters.get("status"), 0);
		return productService.page(Condition.getPage(query), Wrappers.<MallProductEntity>lambdaQuery()
			.eq(MallProductEntity::getIsDeleted, 0)
			.and(Func.isNotBlank(keyword), wrapper -> wrapper
				.like(MallProductEntity::getProductName, keyword)
				.or().like(MallProductEntity::getProductCode, keyword)
				.or().like(MallProductEntity::getMerchantName, keyword))
			.eq(Func.isNotBlank(fulfillmentType), MallProductEntity::getFulfillmentType, fulfillmentType)
			.eq(Func.isNotBlank(categoryCode), MallProductEntity::getCategoryCode, categoryCode)
			.eq(status != null, MallProductEntity::getStatus, status)
			.orderByDesc(MallProductEntity::getSortNo)
			.orderByDesc(MallProductEntity::getUpdateTime));
	}

	public MallProductEntity detail(Long id) {
		MallProductEntity product = productService.getById(id);
		if (product == null || Func.equals(product.getIsDeleted(), 1)) throw new ServiceException("商品不存在");
		return product;
	}

	@Transactional(rollbackFor = Exception.class)
	public MallProductEntity save(Map<String, Object> body) {
		Long id = Func.toLong(body.get("id"));
		MallProductEntity product = id == null ? new MallProductEntity() : detail(id);
		String productName = Func.toStr(body.get("productName"), "").trim();
		if (Func.isBlank(productName)) throw new ServiceException("商品名称不能为空");
		String fulfillmentType = normalizeFulfillment(body.get("fulfillmentType"));
		int salePoints = Func.toInt(body.get("salePoints"), 0);
		if (salePoints <= 0) throw new ServiceException("兑换绿豆必须大于0");
		int stockAvailable = Math.max(0, Func.toInt(body.get("stockAvailable"), 0));
		int stockTotal = Math.max(stockAvailable, Func.toInt(body.get("stockTotal"), stockAvailable));
		int maxQty = Math.max(1, Func.toInt(body.get("maxQtyPerOrder"), 1));
		int perUserLimit = Math.max(0, Func.toInt(body.get("perUserLimit"), 0));
		if (perUserLimit > 0 && maxQty > perUserLimit) throw new ServiceException("单次最大数量不能超过每人累计限兑数量");
		if ("PICKUP".equals(fulfillmentType) && Func.isBlank(Func.toStr(body.get("pickupAddress"), ""))) {
			throw new ServiceException("到店领取商品必须配置领取地址");
		}

		product.setProductCode(Func.isBlank(Func.toStr(body.get("productCode"), ""))
			? generateCode() : Func.toStr(body.get("productCode"), "").trim());
		product.setProductName(productName);
		product.setProductDesc(Func.toStr(body.get("productDesc"), "").trim());
		product.setProductType(normalizeProductType(body.get("productType")));
		product.setCoverUrl(Func.toStr(body.get("coverUrl"), "").trim());
		product.setGalleryJson(toJsonArray(body.get("gallery")));
		product.setCategoryCode(Func.toStr(body.get("categoryCode"), "").trim());
		product.setCategoryName(Func.toStr(body.get("categoryName"), "").trim());
		product.setSpecJson(toJsonArray(body.get("specs")));
		product.setExchangeNotice(Func.toStr(body.get("exchangeNotice"), "").trim());
		product.setSalePoints(salePoints);
		product.setMarketAmount(Math.max(0, Func.toInt(body.get("marketAmount"), 0)));
		product.setStockTotal(stockTotal);
		product.setStockAvailable(stockAvailable);
		if (product.getSoldQty() == null) product.setSoldQty(0);
		product.setFulfillmentType(fulfillmentType);
		product.setMerchantId(Func.toLong(body.get("merchantId")));
		product.setMerchantName(Func.toStr(body.get("merchantName"), "").trim());
		product.setPickupAddress(Func.toStr(body.get("pickupAddress"), "").trim());
		product.setPerUserLimit(perUserLimit);
		product.setMaxQtyPerOrder(maxQty);
		product.setRequireAddress("SHIP".equals(fulfillmentType) ? (Func.toInt(body.get("requireAddress"), 1) == 0 ? 0 : 1) : 0);
		product.setSortNo(Func.toInt(body.get("sortNo"), 0));
		product.setStatus(Func.toInt(body.get("status"), 0) == 1 ? 1 : 0);
		if (product.getStatus() == 1 && product.getPublishedAt() == null) product.setPublishedAt(new Date());
		if (id == null) productService.save(product); else productService.updateById(product);
		return product;
	}

	@Transactional(rollbackFor = Exception.class)
	public MallProductEntity changeStatus(Long id, Integer status) {
		MallProductEntity product = detail(id);
		if (status != null && status == 1) {
			validatePublish(product);
			product.setStatus(1);
			if (product.getPublishedAt() == null) product.setPublishedAt(new Date());
		} else {
			product.setStatus(0);
		}
		productService.updateById(product);
		return product;
	}

	private void validatePublish(MallProductEntity product) {
		if (Func.isBlank(product.getProductName())) throw new ServiceException("商品名称不能为空");
		if (Func.isBlank(product.getCoverUrl())) throw new ServiceException("请先上传商品主图");
		if (Func.toInt(product.getSalePoints(), 0) <= 0) throw new ServiceException("兑换绿豆配置不正确");
		if (Func.toInt(product.getStockAvailable(), 0) <= 0) throw new ServiceException("当前没有可兑换库存");
		if ("PICKUP".equalsIgnoreCase(product.getFulfillmentType()) && Func.isBlank(product.getPickupAddress())) {
			throw new ServiceException("请先配置到店领取地址");
		}
	}

	private String normalizeFulfillment(Object value) {
		String type = Func.toStr(value, "SHIP").trim().toUpperCase(Locale.ROOT);
		return "PICKUP".equals(type) || "VIRTUAL".equals(type) ? type : "SHIP";
	}

	private String normalizeProductType(Object value) {
		String type = Func.toStr(value, "OTHER").trim().toUpperCase(Locale.ROOT);
		if ("COUPON".equals(type)) throw new ServiceException("优惠券不能作为商城商品，请在优惠券模块维护");
		return Func.isBlank(type) ? "OTHER" : type;
	}

	private String toJsonArray(Object value) {
		if (value == null) return null;
		List<String> values = new ArrayList<>();
		if (value instanceof List<?>) {
			for (Object item : (List<?>) value) {
				String text = extractValue(item);
				if (Func.isNotBlank(text)) values.add(text.trim());
			}
		} else {
			String raw = String.valueOf(value).trim();
			if (raw.startsWith("[") && raw.endsWith("]")) {
				try {
					List<String> parsed = JSON.parseArray(raw, String.class);
					if (parsed != null) values.addAll(parsed);
				} catch (Exception ignored) {
					values.add(raw);
				}
			} else if (Func.isNotBlank(raw)) {
				Collections.addAll(values, raw.split("[,，\\n]"));
			}
		}
		Set<String> unique = new LinkedHashSet<>();
		for (String item : values) if (Func.isNotBlank(item)) unique.add(item.trim());
		return unique.isEmpty() ? null : JSON.toJSONString(unique);
	}

	private String extractValue(Object item) {
		if (item == null) return "";
		if (item instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) item;
			Object value = map.get("value");
			if (value == null) value = map.get("name");
			if (value == null) value = map.get("label");
			return Func.toStr(value, "");
		}
		return String.valueOf(item);
	}

	private String generateCode() {
		return "MP" + System.currentTimeMillis();
	}
}
