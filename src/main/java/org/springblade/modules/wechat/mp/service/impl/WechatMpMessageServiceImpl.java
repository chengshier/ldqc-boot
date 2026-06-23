package org.springblade.modules.wechat.mp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.modules.wechat.config.WechatMpProperties;
import org.springblade.modules.wechat.mp.dto.BatchSendResult;
import org.springblade.modules.wechat.mp.dto.SubscribeMsgCmd;
import org.springblade.modules.wechat.mp.dto.TemplateMsgCmd;
import org.springblade.modules.wechat.mp.dto.WechatSendResult;
import org.springblade.modules.wechat.mp.service.WechatMpMessageService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatMpMessageServiceImpl implements WechatMpMessageService {

	private final WechatMpProperties mpProperties;
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final RestTemplate restTemplate = new RestTemplate();

	@Override
	public WechatSendResult sendTemplateMessage(TemplateMsgCmd cmd) {
		validateTemplateCmd(cmd);
		return doSendTemplateMessage(cmd, false);
	}

	@Override
	public WechatSendResult sendSubscribeMessage(SubscribeMsgCmd cmd) {
		validateSubscribeCmd(cmd);
		return doSendSubscribeMessage(cmd, false);
	}

	@Override
	public BatchSendResult batchSendTemplate(List<TemplateMsgCmd> cmds) {
		if (cmds == null || cmds.isEmpty()) {
			throw new ServiceException("批量发送参数不能为空");
		}
		List<WechatSendResult> details = new ArrayList<>();
		int success = 0;
		for (TemplateMsgCmd cmd : cmds) {
			try {
				WechatSendResult result = sendTemplateMessage(cmd);
				details.add(result);
				if (result.isSuccess()) {
					success++;
				}
			} catch (Exception ex) {
				WechatSendResult fail = new WechatSendResult();
				fail.setSuccess(false);
				fail.setErrCode("BATCH_LOCAL_EXCEPTION");
				fail.setErrMsg(ex.getMessage());
				fail.setBizType(cmd == null ? null : cmd.getBizType());
				fail.setBizId(cmd == null ? null : cmd.getBizId());
				details.add(fail);
			}
		}
		BatchSendResult result = new BatchSendResult();
		result.setTotal(cmds.size());
		result.setSuccessCount(success);
		result.setFailCount(cmds.size() - success);
		result.setDetails(details);
		return result;
	}

	private WechatSendResult doSendTemplateMessage(TemplateMsgCmd cmd, boolean retried) {
		String accessToken = getAccessToken();
		String url = mpProperties.getApiBaseUrl() + "/cgi-bin/message/template/send?access_token=" + accessToken;
		Map<String, Object> body = new HashMap<>();
		body.put("touser", cmd.getToUserOpenId());
		body.put("template_id", cmd.getTemplateId());
		body.put("url", cmd.getUrl());
		body.put("miniprogram", buildMiniprogramNode(cmd.getAppId(), cmd.getPagePath()));
		body.put("data", cmd.getData());

		JsonNode json = post(url, body);
		if (tokenExpired(json) && !retried) {
			refreshAccessToken(true);
			return doSendTemplateMessage(cmd, true);
		}
		return toSendResult(json, cmd.getBizType(), cmd.getBizId());
	}

	private WechatSendResult doSendSubscribeMessage(SubscribeMsgCmd cmd, boolean retried) {
		String accessToken = getAccessToken();
		String url = mpProperties.getApiBaseUrl() + "/cgi-bin/message/subscribe/send?access_token=" + accessToken;
		Map<String, Object> body = new HashMap<>();
		body.put("touser", cmd.getToUserOpenId());
		body.put("template_id", cmd.getTemplateId());
		body.put("page", cmd.getPage());
		body.put("data", cmd.getData());
		body.put("lang", cmd.getLang() == null ? "zh_CN" : cmd.getLang());

		JsonNode json = post(url, body);
		if (tokenExpired(json) && !retried) {
			refreshAccessToken(true);
			return doSendSubscribeMessage(cmd, true);
		}
		return toSendResult(json, cmd.getBizType(), cmd.getBizId());
	}

	private Map<String, String> buildMiniprogramNode(String appId, String pagePath) {
		if (isBlank(appId) || isBlank(pagePath)) {
			return null;
		}
		Map<String, String> map = new HashMap<>();
		map.put("appid", appId);
		map.put("pagepath", pagePath);
		return map;
	}

	private WechatSendResult toSendResult(JsonNode json, String bizType, String bizId) {
		WechatSendResult result = new WechatSendResult();
		String errCode = text(json, "errcode");
		String errMsg = text(json, "errmsg");
		result.setSuccess("0".equals(errCode));
		result.setErrCode(errCode);
		result.setErrMsg(errMsg);
		result.setMsgId(text(json, "msgid"));
		result.setBizType(bizType);
		result.setBizId(bizId);
		return result;
	}

	private JsonNode post(String url, Object body) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Object> entity = new HttpEntity<>(body, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
			return objectMapper.readTree(response.getBody());
		} catch (Exception e) {
			log.error("wechat mp request failed, url={}, err={}", url, e.getMessage(), e);
			throw new ServiceException("微信公众号接口请求失败: " + e.getMessage());
		}
	}

	private String getAccessToken() {
		String cacheKey = buildTokenCacheKey();
		String token = redisTemplate.opsForValue().get(cacheKey);
		if (!isBlank(token)) {
			return token;
		}
		return refreshAccessToken(false);
	}

	private String refreshAccessToken(boolean forceRefresh) {
		String cacheKey = buildTokenCacheKey();
		if (forceRefresh) {
			redisTemplate.delete(cacheKey);
		}
		String url = mpProperties.getApiBaseUrl() + "/cgi-bin/token?grant_type=client_credential&appid="
			+ mpProperties.getAppId() + "&secret=" + mpProperties.getAppSecret();
		try {
			String resp = restTemplate.getForObject(url, String.class);
			JsonNode json = objectMapper.readTree(resp);
			String accessToken = text(json, "access_token");
			if (isBlank(accessToken)) {
				throw new ServiceException("获取公众号 access_token 失败: " + text(json, "errmsg"));
			}
			long expiresIn = json.path("expires_in").asLong(7200);
			// 预留 5 分钟过期冗余，规避边界失效
			long cacheSeconds = Math.max(300, expiresIn - 300);
			redisTemplate.opsForValue().set(cacheKey, accessToken, cacheSeconds, TimeUnit.SECONDS);
			return accessToken;
		} catch (Exception e) {
			throw new ServiceException("获取公众号 access_token 异常: " + e.getMessage());
		}
	}

	private boolean tokenExpired(JsonNode json) {
		String errCode = text(json, "errcode");
		return "40001".equals(errCode) || "42001".equals(errCode);
	}

	private String buildTokenCacheKey() {
		return mpProperties.getTokenCachePrefix() + mpProperties.getAppId();
	}

	private void validateTemplateCmd(TemplateMsgCmd cmd) {
		if (cmd == null) {
			throw new ServiceException("模板消息参数不能为空");
		}
		if (isBlank(cmd.getToUserOpenId()) || isBlank(cmd.getTemplateId()) || cmd.getData() == null || cmd.getData().isEmpty()) {
			throw new ServiceException("模板消息参数不完整：toUserOpenId/templateId/data 必填");
		}
	}

	private void validateSubscribeCmd(SubscribeMsgCmd cmd) {
		if (cmd == null) {
			throw new ServiceException("订阅消息参数不能为空");
		}
		if (isBlank(cmd.getToUserOpenId()) || isBlank(cmd.getTemplateId()) || cmd.getData() == null || cmd.getData().isEmpty()) {
			throw new ServiceException("订阅消息参数不完整：toUserOpenId/templateId/data 必填");
		}
	}

	private String text(JsonNode json, String field) {
		if (json == null || json.get(field) == null || json.get(field).isNull()) {
			return null;
		}
		return json.get(field).asText();
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}

