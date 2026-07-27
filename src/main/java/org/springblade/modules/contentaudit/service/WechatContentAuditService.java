package org.springblade.modules.contentaudit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.userthree.pojo.entity.UserThreeEntity;
import org.springblade.modules.userthree.service.IUserThreeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** WeChat text security check for a real mini-program user. */
@Slf4j @Service
public class WechatContentAuditService {
	public static final byte PASSED = 1, REJECTED = 2, RETRY = 3;
	private final IUserThreeService userThreeService; private final StringRedisTemplate redis;
	private final RestTemplate http = new RestTemplate(); private final ObjectMapper json = new ObjectMapper();
	@Value("${WECHAT.app-id}") private String appId;
	@Value("${WECHAT.app-secret}") private String appSecret;
	public WechatContentAuditService(IUserThreeService userThreeService, StringRedisTemplate redis) { this.userThreeService = userThreeService; this.redis = redis; }
	public AuditResult audit(Long userId, String content) {
		try {
			UserThreeEntity account = userThreeService.lambdaQuery().eq(UserThreeEntity::getUserId, userId).eq(UserThreeEntity::getSource, "wechat_mini").one();
			if (account == null || account.getOauthId() == null) return AuditResult.retry("未找到微信登录身份");
			Map<String,Object> body = new LinkedHashMap<>(); body.put("content", content); body.put("version", 2); body.put("scene", 2); body.put("openid", account.getOauthId());
			HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
			JsonNode result = json.readTree(http.postForObject("https://api.weixin.qq.com/wxa/msg_sec_check?access_token=" + token(), new HttpEntity<>(body, headers), String.class));
			if (result.path("errcode").asInt(-1) != 0) return AuditResult.retry(result.path("errmsg").asText("微信审核调用失败"));
			return "pass".equalsIgnoreCase(result.path("result").path("suggest").asText()) ? AuditResult.pass() : AuditResult.reject("评论内容未通过平台审核");
		} catch (Exception e) { log.error("wechat comment audit failed", e); return AuditResult.retry("审核服务暂不可用"); }
	}
	private String token() throws Exception {
		String cached = redis.opsForValue().get("wechat:content-audit:access-token"); if (cached != null && !cached.isBlank()) return cached;
		JsonNode response = json.readTree(http.getForObject("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}", String.class, appId, appSecret));
		String token = response.path("access_token").asText(); if (token.isBlank()) throw new IllegalStateException(response.path("errmsg").asText());
		redis.opsForValue().set("wechat:content-audit:access-token", token, Math.max(300, response.path("expires_in").asLong(7200) - 300), TimeUnit.SECONDS); return token;
	}
	public record AuditResult(byte status, String reason) { static AuditResult pass(){return new AuditResult(PASSED,null);} static AuditResult reject(String r){return new AuditResult(REJECTED,r);} static AuditResult retry(String r){return new AuditResult(RETRY,r);} }
}
