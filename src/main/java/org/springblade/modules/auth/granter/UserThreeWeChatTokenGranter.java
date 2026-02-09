/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.modules.auth.granter;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.CommonConstant;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.oauth2.exception.UserInvalidException;
import org.springblade.core.oauth2.granter.AbstractTokenGranter;
import org.springblade.core.oauth2.handler.PasswordHandler;
import org.springblade.core.oauth2.provider.OAuth2Request;
import org.springblade.core.oauth2.service.OAuth2ClientService;
import org.springblade.core.oauth2.service.OAuth2User;
import org.springblade.core.oauth2.service.OAuth2UserService;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.modules.auth.utils.TokenUtil;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.pojo.entity.UserInfo;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.userthree.pojo.entity.UserThreeEntity;
import org.springblade.modules.userthree.service.IUserThreeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * UserThree WeChat Mini Program TokenGranter
 *
 * @author BladeX
 */
@Slf4j
@Component
public class UserThreeWeChatTokenGranter extends AbstractTokenGranter {

	public static final String GRANT_TYPE = "wechat_mini";
	private static final String SOURCE = "wechat_mini";
	private static final String WECHAT_API_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

	private final IUserThreeService userThreeService;
	private final IUserService userService;
	private final RestTemplate restTemplate;

	@Value("${WECHAT.app-id}")
	private String appId;

	@Value("${WECHAT.app-secret}")
	private String appSecret;

	public UserThreeWeChatTokenGranter(OAuth2ClientService clientService, OAuth2UserService userService,
									   PasswordHandler passwordHandler, IUserThreeService userThreeService,
									   IUserService systemUserService) {
		super(clientService, userService, passwordHandler);
		this.userThreeService = userThreeService;
		this.userService = systemUserService;
		this.restTemplate = new RestTemplate();
	}

	@Override
	public String type() {
		return GRANT_TYPE;
	}

	@Override
	public OAuth2User user(OAuth2Request request) {
		String code = request.getCode();
		if (StringUtil.isBlank(code)) {
			throw new UserInvalidException("Please provide the WeChat Mini Program code.");
		}

		String openid;
		String sessionKey;
		String unionid;

		// MOCK LOGIC: If the code is "the code is a mock one", skip WeChat API call
		if ("the code is a mock one".equals(code)) {
			openid = "mock_openid_" + StringUtil.randomUUID().substring(0, 8);
			sessionKey = "mock_session_key_" + StringUtil.randomUUID().substring(0, 8);
			unionid = "mock_unionid_" + StringUtil.randomUUID().substring(0, 8);
			log.info("Using MOCK mode for WeChat Login. OpenID: {}", openid);
		} else {
			// 1. Call WeChat API to get openid and session_key
			Map<String, String> params = new HashMap<>();
			params.put("appid", appId);
			params.put("secret", appSecret);
			params.put("code", code);

			String responseBody;
			try {
				responseBody = restTemplate.getForObject(WECHAT_API_URL, String.class, appId, appSecret, code);
			} catch (Exception e) {
				log.error("Failed to call WeChat API", e);
				throw new UserInvalidException("Failed to call WeChat API: " + e.getMessage());
			}

			if (StringUtil.isBlank(responseBody)) {
				throw new UserInvalidException("WeChat API response is empty.");
			}

			try {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree(responseBody);
				if (root.has("errcode") && root.get("errcode").asInt() != 0) {
					throw new UserInvalidException("WeChat API Error: " + root.get("errmsg").asText());
				}
				openid = root.get("openid").asText();
				sessionKey = root.get("session_key").asText();
				unionid = root.has("unionid") ? root.get("unionid").asText() : null;
			} catch (Exception e) {
				log.error("Failed to parse WeChat API response", e);
				throw new UserInvalidException("Failed to parse WeChat API response.");
			}
		}

		if (StringUtil.isBlank(openid)) {
			throw new UserInvalidException("Could not retrieve OpenID from WeChat.");
		}

		// 2. Check if user exists in UserThreeEntity
		UserThreeEntity userThree = userThreeService.getOne(Wrappers.<UserThreeEntity>lambdaQuery()
			.eq(UserThreeEntity::getOauthId, openid)
			.eq(UserThreeEntity::getSource, SOURCE));

		Long userId;

		if (userThree != null) {
			// Update session key
			userThree.setAccessToken(sessionKey);
			if (StringUtil.isNotBlank(unionid)) {
				userThree.setUnionId(unionid);
			}
			userThreeService.updateById(userThree);
			userId = userThree.getUserId();
		} else {
			// 3. Create new user
			User user = new User();
			user.setTenantId(request.getTenantId());
			user.setAccount(SOURCE + "_" + StringUtil.randomUUID().substring(0, 8)); // Generate random account
			user.setName(SOURCE + "_" + StringUtil.randomUUID().substring(0, 8)); // Generate random account
//			user.setRealName("WeChat User");
			user.setRoleId("1123598816738675202");
			user.setDeptId("2019301872048885762");
			user.setPostId("2019302154942107649");
			user.setPassword(StringUtil.randomUUID()); // Random password
			user.setStatus(1); // Active

			boolean userCreated = userService.submit(user);
			if (!userCreated) {
				throw new ServiceException("Failed to create system user.");
			}
			userId = user.getId();

			// Create UserThreeEntity
			userThree = new UserThreeEntity();
			userThree.setUserId(userId);
			userThree.setTenantId(user.getTenantId());
			userThree.setOauthId(openid);
			userThree.setUnionId(unionid);
			userThree.setSource(SOURCE);
			userThree.setAccessToken(sessionKey);
			userThree.setUsername("WeChat User");

			userThreeService.save(userThree);
		}

		// 4. Load UserInfo
		UserInfo userInfo = userService.userInfo(userId);
		if (userInfo == null) {
			throw new UserInvalidException("User info not found.");
		}

		// Fix: If roles are empty (e.g. new user), assign "guest" role to pass authentication checks
		if (Func.isEmpty(userInfo.getRoles())) {
			userInfo.setRoles(Collections.singletonList("guest"));
		}

		// 5. Convert to OAuth2User
		OAuth2User oauth2User = TokenUtil.convertUser(userInfo, request);
		oauth2User.setClient(client(request));
		return oauth2User;
	}
}
