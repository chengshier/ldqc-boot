package org.springblade.modules.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.mp")
public class WechatMpProperties {

	private String appId;
	private String appSecret;
	private String tokenCachePrefix = "wechat:mp:access-token:";
	private String apiBaseUrl = "https://api.weixin.qq.com";
}

