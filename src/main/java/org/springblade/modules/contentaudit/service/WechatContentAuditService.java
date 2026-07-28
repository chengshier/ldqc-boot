package org.springblade.modules.contentaudit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.userthree.pojo.entity.UserThreeEntity;
import org.springblade.modules.userthree.service.IUserThreeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信小程序官方内容安全接口。
 *
 * <p>文本使用 msg_sec_check 同步判断；图片与社区短视频封面使用
 * media_check_async 异步检测。这里只接入微信官方能力，不调用腾讯云或其他收费审核服务。</p>
 */
@Slf4j
@Service
public class WechatContentAuditService {

	public static final byte PROCESSING = 0;
	public static final byte PASSED = 1;
	public static final byte REJECTED = 2;
	public static final byte RETRY = 3;
	public static final byte MANUAL_REQUIRED = 4;

	/** 微信场景枚举：2 评论；3 论坛/UGC。 */
	public static final int SCENE_COMMENT = 2;
	public static final int SCENE_UGC = 3;

	private static final String TOKEN_CACHE_KEY = "wechat:content-audit:access-token";
	private static final String TEXT_CHECK_URL = "https://api.weixin.qq.com/wxa/msg_sec_check?access_token=";
	private static final String MEDIA_CHECK_URL = "https://api.weixin.qq.com/wxa/media_check_async?access_token=";

	private final IUserThreeService userThreeService;
	private final StringRedisTemplate redis;
	private final RestTemplate http = new RestTemplate();
	private final ObjectMapper json = new ObjectMapper();

	@Value("${WECHAT.app-id}")
	private String appId;

	@Value("${WECHAT.app-secret}")
	private String appSecret;

	public WechatContentAuditService(IUserThreeService userThreeService, StringRedisTemplate redis) {
		this.userThreeService = userThreeService;
		this.redis = redis;
	}

	/** 兼容原评论审核调用。 */
	public AuditResult audit(Long userId, String content) {
		return auditText(userId, content, SCENE_COMMENT);
	}

	/** 默认按评论场景审核文本。 */
	public AuditResult auditText(Long userId, String content) {
		return auditText(userId, content, SCENE_COMMENT);
	}

	/** 同步检测动态文案、评论等文本。 */
	public AuditResult auditText(Long userId, String content, int scene) {
		try {
			String openId = resolveOpenId(userId);
			if (openId == null) {
				return AuditResult.retry("未找到微信小程序登录身份");
			}
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("content", content == null ? "" : content);
			body.put("version", 2);
			body.put("scene", normalizeScene(scene));
			body.put("openid", openId);
			JsonNode response = postJson(TEXT_CHECK_URL + token(), body);
			if (response.path("errcode").asInt(-1) != 0) {
				return AuditResult.retry(response.path("errmsg").asText("微信文本审核调用失败"));
			}
			return fromSuggest(response.path("result").path("suggest").asText(),
				response.path("trace_id").asText(null), "文本内容");
		} catch (Exception exception) {
			log.error("微信文本内容审核失败，userId={}，scene={}", userId, scene, exception);
			return AuditResult.retry("微信文本审核服务暂不可用");
		}
	}

	/** 默认按评论场景提交媒体。动态内容应显式传入 SCENE_UGC。 */
	public AuditResult submitMedia(Long userId, String mediaUrl, int mediaType) {
		return submitMedia(userId, mediaUrl, mediaType, SCENE_COMMENT);
	}

	/**
	 * 提交图片或视频封面异步审核。
	 *
	 * @param mediaType 1=音频，2=图片；本项目第一阶段只提交图片，因此固定传 2。
	 */
	public AuditResult submitMedia(Long userId, String mediaUrl, int mediaType, int scene) {
		if (!isHttpUrl(mediaUrl)) {
			return AuditResult.manual("媒体地址不是微信可访问的 HTTP/HTTPS 地址");
		}
		try {
			String openId = resolveOpenId(userId);
			if (openId == null) {
				return AuditResult.retry("未找到微信小程序登录身份");
			}
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("media_url", mediaUrl.trim());
			body.put("media_type", mediaType);
			body.put("version", 2);
			body.put("scene", normalizeScene(scene));
			body.put("openid", openId);
			JsonNode response = postJson(MEDIA_CHECK_URL + token(), body);
			if (response.path("errcode").asInt(-1) != 0) {
				return AuditResult.retry(response.path("errmsg").asText("微信媒体审核提交失败"));
			}
			String traceId = response.path("trace_id").asText("").trim();
			if (traceId.isEmpty()) {
				return AuditResult.retry("微信媒体审核未返回 trace_id");
			}
			return AuditResult.processing(traceId, "已提交微信媒体异步审核");
		} catch (Exception exception) {
			log.error("微信媒体内容审核提交失败，userId={}，scene={}，mediaUrl={}", userId, scene, mediaUrl, exception);
			return AuditResult.retry("微信媒体审核服务暂不可用");
		}
	}

	/** 将微信回调的 suggest 转成平台统一状态。 */
	public AuditResult fromSuggest(String suggest, String traceId, String contentName) {
		String value = suggest == null ? "" : suggest.trim().toLowerCase(Locale.ROOT);
		if ("pass".equals(value)) {
			return AuditResult.pass(traceId);
		}
		if ("risky".equals(value)) {
			return AuditResult.reject(contentName + "未通过微信内容安全审核", traceId);
		}
		if ("review".equals(value)) {
			return AuditResult.manual(contentName + "需要运营人员复核", traceId);
		}
		return AuditResult.manual("微信返回未知审核建议：" + (value.isEmpty() ? "empty" : value), traceId);
	}

	private JsonNode postJson(String url, Map<String, Object> body) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String response = http.postForObject(url, new HttpEntity<>(body, headers), String.class);
		return json.readTree(response == null ? "{}" : response);
	}

	private String resolveOpenId(Long userId) {
		if (userId == null || userId <= 0) {
			return null;
		}
		UserThreeEntity account = userThreeService.lambdaQuery()
			.eq(UserThreeEntity::getUserId, userId)
			.eq(UserThreeEntity::getSource, "wechat_mini")
			.last("limit 1")
			.one();
		return account == null || account.getOauthId() == null || account.getOauthId().isBlank()
			? null : account.getOauthId().trim();
	}

	private String token() throws Exception {
		String cached = redis.opsForValue().get(TOKEN_CACHE_KEY);
		if (cached != null && !cached.isBlank()) {
			return cached;
		}
		JsonNode response = json.readTree(http.getForObject(
			"https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}",
			String.class, appId, appSecret));
		String accessToken = response.path("access_token").asText();
		if (accessToken.isBlank()) {
			throw new IllegalStateException(response.path("errmsg").asText("获取微信 access_token 失败"));
		}
		redis.opsForValue().set(TOKEN_CACHE_KEY, accessToken,
			Math.max(300, response.path("expires_in").asLong(7200) - 300), TimeUnit.SECONDS);
		return accessToken;
	}

	private int normalizeScene(int scene) {
		return scene >= 1 && scene <= 4 ? scene : SCENE_COMMENT;
	}

	private boolean isHttpUrl(String value) {
		if (value == null) return false;
		String lower = value.trim().toLowerCase(Locale.ROOT);
		return lower.startsWith("https://") || lower.startsWith("http://");
	}

	public record AuditResult(byte status, String reason, String traceId) {
		public static AuditResult processing(String traceId, String reason) {
			return new AuditResult(PROCESSING, reason, traceId);
		}
		public static AuditResult pass() {
			return pass(null);
		}
		public static AuditResult pass(String traceId) {
			return new AuditResult(PASSED, null, traceId);
		}
		public static AuditResult reject(String reason) {
			return reject(reason, null);
		}
		public static AuditResult reject(String reason, String traceId) {
			return new AuditResult(REJECTED, reason, traceId);
		}
		public static AuditResult retry(String reason) {
			return new AuditResult(RETRY, reason, null);
		}
		public static AuditResult manual(String reason) {
			return manual(reason, null);
		}
		public static AuditResult manual(String reason, String traceId) {
			return new AuditResult(MANUAL_REQUIRED, reason, traceId);
		}
	}
}
