package org.springblade.modules.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WechatMpService {
	@Value("${WECHAT.app-id}")
	private String appId;
	@Value("${WECHAT.app-secret}")
	private String appSecret;

	// 使用Redis缓存token，有效期2小时（7200秒）
	private static final String ACCESS_TOKEN_KEY = "whehfkahkdhaskh";

	private final StringRedisTemplate redisTemplate;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	public WechatMpService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.restTemplate = new RestTemplate(); // 或从Spring容器注入
	}

	/**
	 * 获取公众号access_token（自动从缓存获取或刷新）
	 */
	public String getAccessToken() {
		String token = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
		if (token != null && !token.isEmpty()) {
			return token;
		}

		// 调用微信接口获取新token
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
			+ appId + "&secret=" + appSecret;

		try {
			String result = restTemplate.getForObject(url, String.class);
			JsonNode json = objectMapper.readTree(result);

			JsonNode tokenNode = json.get("access_token");
			if (tokenNode != null) {
				String newToken = tokenNode.asText();
				int expiresIn = json.get("expires_in").asInt();

				// 存入redis，过期时间比微信提前5分钟，避免边界失效
				redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, newToken,
					expiresIn - 300, TimeUnit.SECONDS);
				return newToken;
			} else {
				throw new RuntimeException("获取access_token失败: " + json.get("errmsg").asText());
			}
		} catch (Exception e) {
			throw new RuntimeException("获取access_token异常", e);
		}
	}

	/**
	 * 获取用户列表（分页）
	 * @param nextOpenid 第一个拉取的OpenID，不填默认从头开始
	 */
	public JsonNode getUserList(String nextOpenid) {
		String token = getAccessToken();
		String nextId = (nextOpenid == null || nextOpenid.isEmpty()) ? "" : nextOpenid;
		String url = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=" + token
			+ "&next_openid=" + nextId;

		try {
			String result = restTemplate.getForObject(url, String.class);
			return objectMapper.readTree(result);
		} catch (Exception e) {
			throw new RuntimeException("获取用户列表失败", e);
		}
	}

	/**
	 * 获取单个用户信息
	 */
	public JsonNode getUserInfo(String openid) {
		String token = getAccessToken();
		String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + token
			+ "&openid=" + openid + "&lang=zh_CN";

		try {
			String result = restTemplate.getForObject(url, String.class);
			return objectMapper.readTree(result);
		} catch (Exception e) {
			throw new RuntimeException("获取用户信息失败", e);
		}
	}

	/**
	 * 发送模板消息
	 */
	public JsonNode sendTemplateMessage(String openid, String templateId,
										Map<String, Object> data,
										String miniAppId, String pagePath) {
		String token = getAccessToken();
		String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;

		Map<String, Object> body = new HashMap<>();
		body.put("touser", openid);
		body.put("template_id", templateId);
		body.put("data", data);

		if (miniAppId != null && !miniAppId.isEmpty()) {
			Map<String, String> miniProgram = new HashMap<>();
			miniProgram.put("appid", miniAppId);
			miniProgram.put("pagepath", pagePath != null ? pagePath : "");
			body.put("miniprogram", miniProgram);
		}

		try {
			String requestBody = objectMapper.writeValueAsString(body);
			String result = restTemplate.postForObject(url, requestBody, String.class);
			return objectMapper.readTree(result);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("发送模板消息参数错误", e);
		} catch (Exception e) {
			throw new RuntimeException("发送模板消息失败", e);
		}
	}
}
