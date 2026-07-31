package org.springblade.modules.contentaudit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.contentaudit.service.DynamicContentAutoAuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信小程序媒体内容安全异步回调。
 *
 * <p>第一阶段使用微信后台的明文消息模式，按照 token、timestamp、nonce 校验签名。
 * 回调只根据微信返回的 trace_id 更新已存在的审核任务，不接受客户端指定内容ID。
 * 同时兼容新版 result.suggest 与旧版 isrisky/status_code 回调字段。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("blade-contentaudit/wechat/media-callback")
public class WechatMediaAuditCallbackController {

	private final DynamicContentAutoAuditService dynamicContentAutoAuditService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${WECHAT.message-token:}")
	private String messageToken;

	@GetMapping
	public String verify(@RequestParam String signature,
						 @RequestParam String timestamp,
						 @RequestParam String nonce,
						 @RequestParam String echostr) {
		verifySignature(signature, timestamp, nonce);
		return echostr;
	}

	@PostMapping
	public String callback(@RequestParam(required = false) String signature,
						   @RequestParam(name = "msg_signature", required = false) String messageSignature,
						   @RequestParam String timestamp,
						   @RequestParam String nonce,
						   @RequestBody(required = false) String body) {
		verifySignature(Func.isNotBlank(signature) ? signature : messageSignature, timestamp, nonce);
		CallbackPayload payload = parsePayload(body);
		if (Func.isBlank(payload.traceId())
			|| Func.isBlank(payload.suggest()) && payload.statusCode() == null) {
			log.warn("微信媒体审核回调缺少 trace_id 或审核结果，body={}", safeBody(body));
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid callback payload");
		}
		Integer normalizedStatusCode = payload.statusCode() == null ? null : payload.statusCode().intValue();
		boolean handled = dynamicContentAutoAuditService.handleMediaCallback(
			payload.traceId(), payload.suggest(), payload.reason(), normalizedStatusCode);
		if (!handled) {
			log.warn("微信媒体审核回调未匹配任务，traceId={}", payload.traceId());
		}
		return "success";
	}

	private void verifySignature(String signature, String timestamp, String nonce) {
		if (Func.isBlank(messageToken)) {
			log.error("未配置 WECHAT.message-token，拒绝微信媒体审核回调");
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "callback token is not configured");
		}
		if (Func.isBlank(signature) || Func.isBlank(timestamp) || Func.isBlank(nonce)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "missing callback signature");
		}
		String[] values = {messageToken.trim(), timestamp.trim(), nonce.trim()};
		Arrays.sort(values);
		String expected = sha1(String.join("", values));
		if (!constantTimeEquals(expected, signature.trim().toLowerCase(Locale.ROOT))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid callback signature");
		}
	}

	private CallbackPayload parsePayload(String body) {
		if (Func.isBlank(body)) {
			return new CallbackPayload(null, null, null, null);
		}
		String value = body.trim();
		if (value.startsWith("{")) {
			try {
				JsonNode root = objectMapper.readTree(value);
				JsonNode result = root.path("result");
				String traceId = firstText(root, "trace_id", "traceId");
				if (Func.isBlank(traceId)) traceId = firstText(result, "trace_id", "traceId");

				String suggest = firstText(result, "suggest");
				if (Func.isBlank(suggest)) suggest = firstText(root, "suggest");
				Long isRisky = firstLong(root, "isrisky", "is_risky");
				if (isRisky == null) isRisky = firstLong(result, "isrisky", "is_risky");
				if (Func.isBlank(suggest) && isRisky != null) {
					suggest = isRisky == 0L ? "pass" : "risky";
				}

				Long statusCode = firstLong(root, "status_code", "statusCode");
				if (statusCode == null) statusCode = firstLong(result, "status_code", "statusCode");
				String label = firstText(result, "label");
				if (Func.isBlank(label)) label = firstText(root, "label");
				String extra = firstText(root, "extra_info_json", "extraInfoJson");
				String reason = buildReason(label, extra, statusCode);
				return new CallbackPayload(traceId, suggest, reason, statusCode);
			} catch (Exception exception) {
				log.warn("解析微信媒体审核 JSON 回调失败", exception);
				return new CallbackPayload(null, null, null, null);
			}
		}

		String traceId = xmlTag(value, "trace_id");
		String suggest = xmlTag(value, "suggest");
		Long isRisky = parseLong(xmlTag(value, "isrisky"));
		if (Func.isBlank(suggest) && isRisky != null) {
			suggest = isRisky == 0L ? "pass" : "risky";
		}
		Long statusCode = parseLong(xmlTag(value, "status_code"));
		String label = xmlTag(value, "label");
		String extra = xmlTag(value, "extra_info_json");
		return new CallbackPayload(traceId, suggest, buildReason(label, extra, statusCode), statusCode);
	}

	private String buildReason(String label, String extra, Long statusCode) {
		StringBuilder builder = new StringBuilder();
		if (statusCode != null && statusCode != 0L) {
			builder.append("微信媒体审核回调异常，status_code=").append(statusCode);
		}
		if (Func.isNotBlank(label)) {
			if (builder.length() > 0) builder.append("；");
			builder.append("审核标签：").append(label.trim());
		}
		if (Func.isNotBlank(extra)) {
			if (builder.length() > 0) builder.append("；");
			builder.append(extra.trim());
		}
		return builder.length() == 0 ? null : builder.toString();
	}

	private String firstText(JsonNode node, String... fields) {
		if (node == null || node.isMissingNode() || node.isNull()) return null;
		for (String field : fields) {
			String value = node.path(field).asText("").trim();
			if (!value.isEmpty()) return value;
		}
		return null;
	}

	private Long firstLong(JsonNode node, String... fields) {
		if (node == null || node.isMissingNode() || node.isNull()) return null;
		for (String field : fields) {
			JsonNode value = node.path(field);
			if (value.isIntegralNumber()) return value.asLong();
			Long parsed = parseLong(value.asText(null));
			if (parsed != null) return parsed;
		}
		return null;
	}

	private Long parseLong(String value) {
		if (Func.isBlank(value)) return null;
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private String xmlTag(String xml, String name) {
		Pattern pattern = Pattern.compile("<" + Pattern.quote(name) + ">(?:<!\\[CDATA\\[)?(.*?)(?:]]>)?</"
			+ Pattern.quote(name) + ">", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(xml);
		return matcher.find() ? matcher.group(1).trim() : null;
	}

	private String sha1(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(bytes.length * 2);
			for (byte item : bytes) builder.append(String.format("%02x", item));
			return builder.toString();
		} catch (Exception exception) {
			throw new IllegalStateException("无法计算微信回调签名", exception);
		}
	}

	private boolean constantTimeEquals(String left, String right) {
		if (left == null || right == null || left.length() != right.length()) return false;
		int result = 0;
		for (int index = 0; index < left.length(); index++) {
			result |= left.charAt(index) ^ right.charAt(index);
		}
		return result == 0;
	}

	private String safeBody(String body) {
		if (body == null) return "";
		return body.length() > 500 ? body.substring(0, 500) : body;
	}

	private record CallbackPayload(String traceId, String suggest, String reason, Long statusCode) {
	}
}
