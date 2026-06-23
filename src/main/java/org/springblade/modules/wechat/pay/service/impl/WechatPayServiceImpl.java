package org.springblade.modules.wechat.pay.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.modules.wechat.config.WechatPayProperties;
import org.springblade.modules.wechat.pay.dto.PayNotifyResult;
import org.springblade.modules.wechat.pay.dto.PayOrderCreateCmd;
import org.springblade.modules.wechat.pay.dto.RefundCreateCmd;
import org.springblade.modules.wechat.pay.dto.WechatOrderStatusResult;
import org.springblade.modules.wechat.pay.dto.WechatPrepayResult;
import org.springblade.modules.wechat.pay.dto.WechatRefundResult;
import org.springblade.modules.wechat.pay.dto.WechatRefundStatusResult;
import org.springblade.modules.wechat.pay.service.WechatPayService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPayServiceImpl implements WechatPayService {

	private static final DateTimeFormatter WX_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private final WechatPayProperties payProperties;
	private final ObjectMapper objectMapper;
	private final RestTemplate restTemplate = new RestTemplate();

	@Override
	public WechatPrepayResult createJsapiOrder(PayOrderCreateCmd cmd) {
		validateOrderCmd(cmd);
		String url = payProperties.getBaseUrl() + "/v3/pay/transactions/jsapi";
		Map<String, Object> request = new HashMap<>();
		request.put("appid", valueOrDefault(cmd.getAppId(), payProperties.getAppId()));
		request.put("mchid", valueOrDefault(cmd.getMchId(), payProperties.getMchId()));
		request.put("description", cmd.getDescription());
		request.put("out_trade_no", cmd.getOutTradeNo());
		request.put("notify_url", valueOrDefault(cmd.getNotifyUrl(), payProperties.getNotifyUrl()));
		request.put("attach", cmd.getAttach());
		request.put("amount", Map.of("total", yuanToFen(cmd.getTotalAmountYuan()), "currency", "CNY"));
		request.put("payer", Map.of("openid", cmd.getOpenid()));
		if (cmd.getClientIp() != null && !cmd.getClientIp().isEmpty()) {
			request.put("scene_info", Map.of("payer_client_ip", cmd.getClientIp()));
		}

		JsonNode respJson = postJson(url, request);
		String prepayId = readText(respJson, "prepay_id");
		if (prepayId == null || prepayId.isEmpty()) {
			throw new ServiceException("微信下单失败，未返回 prepay_id");
		}

		WechatPrepayResult result = new WechatPrepayResult();
		result.setPrepayId(prepayId);
		result.setAppId(valueOrDefault(cmd.getAppId(), payProperties.getAppId()));
		result.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
		result.setNonceStr(UUID.randomUUID().toString().replace("-", ""));
		result.setPkg("prepay_id=" + prepayId);
		result.setSignType("HMAC-SHA256");
		result.setPaySign(signMiniProgramPay(result));
		return result;
	}

	@Override
	public PayNotifyResult verifyAndParsePayNotify(String body, Map<String, String> headers) {
		if (body == null || body.isEmpty()) {
			throw new ServiceException("支付回调报文为空");
		}
		try {
			JsonNode root = objectMapper.readTree(body);
			JsonNode resource = root.get("resource");
			String plainTradeJson;
			if (resource != null && !resource.isNull()) {
				// v3 回调报文，先解密再解析业务字段
				plainTradeJson = decryptV3Resource(resource);
			} else {
				// 兼容明文场景（通常是测试或代理层已解密）
				plainTradeJson = body;
			}
			JsonNode trade = objectMapper.readTree(plainTradeJson);
			PayNotifyResult result = new PayNotifyResult();
			result.setOutTradeNo(readText(trade, "out_trade_no"));
			result.setTransactionId(readText(trade, "transaction_id"));
			result.setTradeState(readText(trade, "trade_state"));
			result.setOpenid(readText(trade.path("payer"), "openid"));
			result.setPayerTotalYuan(fenNodeToYuan(trade.path("amount").path("payer_total")));
			String successTime = readText(trade, "success_time");
			if (successTime != null && !successTime.isEmpty()) {
				result.setSuccessTime(LocalDateTime.parse(successTime, WX_TIME_FORMATTER));
			}
			result.setRawBody(plainTradeJson);
			return result;
		} catch (Exception e) {
			throw new ServiceException("支付回调验签/解析失败: " + e.getMessage());
		}
	}

	@Override
	public WechatOrderStatusResult queryOrder(String outTradeNo) {
		if (outTradeNo == null || outTradeNo.isEmpty()) {
			throw new ServiceException("outTradeNo 不能为空");
		}
		String url = payProperties.getBaseUrl() + "/v3/pay/transactions/out-trade-no/" + outTradeNo + "?mchid=" + payProperties.getMchId();
		JsonNode json = getJson(url);
		WechatOrderStatusResult result = new WechatOrderStatusResult();
		result.setOutTradeNo(readText(json, "out_trade_no"));
		result.setTransactionId(readText(json, "transaction_id"));
		result.setTradeState(readText(json, "trade_state"));
		result.setTotalAmountYuan(fenNodeToYuan(json.path("amount").path("total")));
		result.setPayerTotalYuan(fenNodeToYuan(json.path("amount").path("payer_total")));
		return result;
	}

	@Override
	public void closeOrder(String outTradeNo) {
		if (outTradeNo == null || outTradeNo.isEmpty()) {
			throw new ServiceException("outTradeNo 不能为空");
		}
		String url = payProperties.getBaseUrl() + "/v3/pay/transactions/out-trade-no/" + outTradeNo + "/close";
		Map<String, Object> request = Map.of("mchid", payProperties.getMchId());
		postJson(url, request);
	}

	@Override
	public WechatRefundResult createRefund(RefundCreateCmd cmd) {
		validateRefundCmd(cmd);
		String url = payProperties.getBaseUrl() + "/v3/refund/domestic/refunds";
		Map<String, Object> request = new HashMap<>();
		request.put("out_trade_no", cmd.getOutTradeNo());
		request.put("out_refund_no", cmd.getOutRefundNo());
		request.put("notify_url", valueOrDefault(cmd.getNotifyUrl(), payProperties.getRefundNotifyUrl()));
		request.put("reason", cmd.getReason());
		request.put("amount", Map.of(
			"refund", yuanToFen(cmd.getRefundAmountYuan()),
			"total", yuanToFen(cmd.getTotalAmountYuan()),
			"currency", "CNY"
		));
		JsonNode json = postJson(url, request);
		WechatRefundResult result = new WechatRefundResult();
		result.setOutRefundNo(readText(json, "out_refund_no"));
		result.setRefundId(readText(json, "refund_id"));
		result.setStatus(readText(json, "status"));
		result.setRefundAmountYuan(fenNodeToYuan(json.path("amount").path("refund")));
		return result;
	}

	@Override
	public WechatRefundStatusResult queryRefund(String outRefundNo) {
		if (outRefundNo == null || outRefundNo.isEmpty()) {
			throw new ServiceException("outRefundNo 不能为空");
		}
		String url = payProperties.getBaseUrl() + "/v3/refund/domestic/refunds/" + outRefundNo;
		JsonNode json = getJson(url);
		WechatRefundStatusResult result = new WechatRefundStatusResult();
		result.setOutRefundNo(readText(json, "out_refund_no"));
		result.setRefundId(readText(json, "refund_id"));
		result.setStatus(readText(json, "status"));
		result.setRefundAmountYuan(fenNodeToYuan(json.path("amount").path("refund")));
		return result;
	}

	private JsonNode postJson(String url, Object requestBody) {
		try {
			HttpHeaders headers = buildWxHeaders("POST", url, requestBody);
			HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			return parseWechatResponse(url, response.getBody());
		} catch (Exception e) {
			log.error("wechat pay post failed, url={}, err={}", url, e.getMessage(), e);
			throw new ServiceException("微信支付请求失败: " + e.getMessage());
		}
	}

	private JsonNode getJson(String url) {
		try {
			HttpHeaders headers = buildWxHeaders("GET", url, null);
			HttpEntity<Void> entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			return parseWechatResponse(url, response.getBody());
		} catch (Exception e) {
			log.error("wechat pay get failed, url={}, err={}", url, e.getMessage(), e);
			throw new ServiceException("微信支付请求失败: " + e.getMessage());
		}
	}

	private JsonNode parseWechatResponse(String url, String respBody) throws Exception {
		JsonNode json = objectMapper.readTree(respBody);
		if (json.has("code") && json.has("message")) {
			throw new ServiceException("微信接口报错[" + json.get("code").asText() + "]: " + json.get("message").asText());
		}
		return json;
	}

	private HttpHeaders buildWxHeaders(String method, String url, Object body) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			String nonceStr = UUID.randomUUID().toString().replace("-", "");
			String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
			String bodyJson = body == null ? "" : objectMapper.writeValueAsString(body);
			URI uri = URI.create(url);
			String canonicalUrl = uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
			String message = method + "\n"
				+ canonicalUrl + "\n"
				+ timestamp + "\n"
				+ nonceStr + "\n"
				+ bodyJson + "\n";
			String signature = signWithMerchantPrivateKey(message);
			String authorization = "WECHATPAY2-SHA256-RSA2048 "
				+ "mchid=\"" + requireValue(payProperties.getMchId(), "wechat.pay.mch-id") + "\","
				+ "nonce_str=\"" + nonceStr + "\","
				+ "timestamp=\"" + timestamp + "\","
				+ "serial_no=\"" + requireValue(payProperties.getMerchantSerialNo(), "wechat.pay.merchant-serial-no") + "\","
				+ "signature=\"" + signature + "\"";
			headers.set("Authorization", authorization);
			headers.set("Accept", "application/json");
			return headers;
		} catch (Exception e) {
			throw new ServiceException("构建微信支付 Authorization 失败: " + e.getMessage());
		}
	}

	private String signWithMerchantPrivateKey(String message) throws Exception {
		PrivateKey privateKey = loadPrivateKey();
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(message.getBytes(StandardCharsets.UTF_8));
		byte[] signed = signature.sign();
		return Base64.getEncoder().encodeToString(signed);
	}

	private PrivateKey loadPrivateKey() throws Exception {
		String pemText = payProperties.getPrivateKeyPem();
		if (isBlank(pemText) && !isBlank(payProperties.getPrivateKeyPath())) {
			pemText = Files.readString(Paths.get(payProperties.getPrivateKeyPath()), StandardCharsets.UTF_8);
		}
		if (isBlank(pemText)) {
			throw new ServiceException("未配置商户私钥: wechat.pay.private-key-path / wechat.pay.private-key-pem");
		}
		String normalized = pemText
			.replace("-----BEGIN PRIVATE KEY-----", "")
			.replace("-----END PRIVATE KEY-----", "")
			.replaceAll("\\s", "");
		byte[] keyBytes = Base64.getDecoder().decode(normalized);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}

	private String signMiniProgramPay(WechatPrepayResult prepayResult) {
		try {
			String signKey = payProperties.getPaySignKey();
			if (signKey == null || signKey.isEmpty()) {
				throw new ServiceException("wechat.pay.pay-sign-key 未配置");
			}
			String signData = prepayResult.getAppId() + "\n"
				+ prepayResult.getTimeStamp() + "\n"
				+ prepayResult.getNonceStr() + "\n"
				+ prepayResult.getPkg() + "\n";
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(signKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] signBytes = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(signBytes);
		} catch (Exception e) {
			throw new ServiceException("生成支付签名失败: " + e.getMessage());
		}
	}

	private String decryptV3Resource(JsonNode resource) throws Exception {
		String nonce = readText(resource, "nonce");
		String associatedData = readText(resource, "associated_data");
		String ciphertext = readText(resource, "ciphertext");
		if (ciphertext == null || nonce == null) {
			throw new ServiceException("支付回调密文结构不完整");
		}
		String apiV3Key = payProperties.getApiV3Key();
		if (apiV3Key == null || apiV3Key.length() != 32) {
			throw new ServiceException("wechat.pay.api-v3-key 未配置或长度不是32位");
		}
		byte[] keyBytes = apiV3Key.getBytes(StandardCharsets.UTF_8);
		byte[] cipherBytes = Base64.getDecoder().decode(ciphertext);
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
		cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
		if (associatedData != null && !associatedData.isEmpty()) {
			cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
		}
		byte[] plainBytes = cipher.doFinal(cipherBytes);
		return new String(plainBytes, StandardCharsets.UTF_8);
	}

	private void validateOrderCmd(PayOrderCreateCmd cmd) {
		if (cmd == null) {
			throw new ServiceException("下单参数不能为空");
		}
		if (isBlank(cmd.getOutTradeNo()) || isBlank(cmd.getDescription()) || isBlank(cmd.getOpenid())) {
			throw new ServiceException("下单参数不完整：outTradeNo/description/openid 必填");
		}
		if (cmd.getTotalAmountYuan() == null || cmd.getTotalAmountYuan().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("下单金额必须大于0");
		}
	}

	private void validateRefundCmd(RefundCreateCmd cmd) {
		if (cmd == null) {
			throw new ServiceException("退款参数不能为空");
		}
		if (isBlank(cmd.getOutTradeNo()) || isBlank(cmd.getOutRefundNo())) {
			throw new ServiceException("退款参数不完整：outTradeNo/outRefundNo 必填");
		}
		if (cmd.getRefundAmountYuan() == null || cmd.getTotalAmountYuan() == null) {
			throw new ServiceException("退款金额参数不完整");
		}
		if (cmd.getRefundAmountYuan().compareTo(BigDecimal.ZERO) <= 0 || cmd.getTotalAmountYuan().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("退款金额必须大于0");
		}
		if (cmd.getRefundAmountYuan().compareTo(cmd.getTotalAmountYuan()) > 0) {
			throw new ServiceException("退款金额不能大于订单总金额");
		}
	}

	private int yuanToFen(BigDecimal amountYuan) {
		return amountYuan.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
	}

	private BigDecimal fenNodeToYuan(JsonNode fenNode) {
		if (fenNode == null || fenNode.isNull() || !fenNode.isNumber()) {
			return null;
		}
		return BigDecimal.valueOf(fenNode.asLong()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
	}

	private String readText(JsonNode node, String fieldName) {
		if (node == null || node.isNull()) {
			return null;
		}
		JsonNode target = node.get(fieldName);
		return target == null || target.isNull() ? null : target.asText();
	}

	private String valueOrDefault(String value, String fallback) {
		return isBlank(value) ? fallback : value;
	}

	private String requireValue(String value, String keyName) {
		if (isBlank(value)) {
			throw new ServiceException("配置缺失: " + keyName);
		}
		return value;
	}

	private boolean isBlank(String str) {
		return str == null || str.trim().isEmpty();
	}
}
