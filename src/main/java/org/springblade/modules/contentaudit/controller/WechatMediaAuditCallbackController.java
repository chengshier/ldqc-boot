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
 * 回调只根据微信返回的 trace_id 更新已存在的审核任务，不接受客户端指定内容ID。</p>
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
		if (Func.isBlank(payload.traceId()) || Func.isBlank(payload.suggest())) {
			log.warn("微信媒体审核回调缺少 trace_id 或 suggest，body={}", safeBody(body));
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid callback payload");
		}
		boolean handled = dynamicContentAutoAuditService.handleMediaCallback(
			payload.traceId(), payload.suggest(), payload.reason());
		if (!handled) {
			log.warn("微信媒体审核回调未匹配任务，traceId={}", payload.traceId());
		}
		// 微信要求成功处理时返回 success；重复回调保持幂等。
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
			return new CallbackPayload(null, null, null);
		}
		String value = body.trim();
		if (value.startsWith("{")) {
			try {
				JsonNode root = objectMapper.readTree(value);
				String traceId = firstText(root, "trace_id", "traceId");
				JsonNode result = root.path("result");
				if (Func.isBlank(traceId)) traceId = firstText(result, "trace_id", "traceId");
				String suggest = firstText(result, "suggest");
				if (Func.isBlank(suggest)) suggest = firstText(root, "suggest");
				String label = firstText(result, "label");
				String reason = Func.isBlank(label) ? null : "微信媒体审核标签：" + label;
				return new CallbackPayload(traceId, suggest, reason);
			} catch (Exception exception) {
				log.warn("解析微信媒体审核 JSON 回调失败", exception);
				return new CallbackPayload(null, null, null);
			}
		}
		// 兼容微信后台选择 XML 消息格式时的基础字段。
		String traceId = xmlTag(value, "trace_id");
		String suggest = xmlTag(value, "suggest");
		String label = xmlTag(value, "label");
		return new CallbackPayload(traceId, suggest,
			Func.isBlank(label) ? null : "微信媒体审核标签：" + label);
	}

	private String firstText(JsonNode node, String... fields) {
		if (node == null || node.isMissingNode() || node.isNull()) return null;
		for (String field : fields) {
			String value = node.path(field).asText("").trim();
			if (!value.isEmpty()) return value;
		}
		return null;
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

	private record CallbackPayload(String traceId, String suggest, String reason) {
	}
}
