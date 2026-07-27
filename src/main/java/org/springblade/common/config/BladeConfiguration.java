/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 */
package org.springblade.common.config;

import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.oauth2.endpoint.OAuth2SocialEndpoint;
import org.springblade.core.oauth2.endpoint.OAuth2TokenEndPoint;
import org.springblade.core.secure.provider.HttpMethod;
import org.springblade.core.secure.registry.SecureRegistry;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.modules.auth.endpoint.Oauth2SmsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Blade 安全与 Web 配置。 */
@Configuration(proxyBeanMethods = false)
public class BladeConfiguration implements WebMvcConfigurer {

	@Bean
	public SecureRegistry secureRegistry() {
		return new SecureRegistry()
			.enabled()
			.strictTokenEnabled()
			.strictHeaderEnabled()
			.skipUrls(
				"/blade-auth/**",
				"/blade-system/tenant/info",
				"/blade-flow/process/resource-view",
				"/blade-flow/process/diagram-view",
				"/blade-flow/manager/check-upload",
				"/doc.html",
				"/swagger-ui.html",
				"/static/**",
				"/webjars/**",
				"/swagger-resources/**",
				"/druid/**",
				"/blade-imgDetail/imgDetail/getHot",
				"/blade-imgDetail/imgDetail/getOne",
				"/blade-recommend/recommendToUserByCF",
				"/blade-recommend/home-feed",
				"/blade-category/category/getTreeCategory",
				"/blade-training/training/mobile-page",
				"/blade-training/training/mobile-detail",
				"/blade-training/training/lesson-play-token",
				"/blade-training/training/video-play",
				"/blade-competition/competition/mobile/page",
				"/blade-competition/competition/mobile/detail",
				"/blade-mallproduct/mallProduct/mobile/page",
				"/blade-mallproduct/mallProduct/mobile/detail",
				"/blade-system/talent/mobile/page",
				"/blade-system/talent/mobile/profile",
				"/blade-system/talent/mobile/content",
				"/blade-talentpost/talentPost/mobile/detail",
				"/blade-venue/venue/mobile/page",
				"/blade-venue/venue/mobile/detail"
			)
			.authEnabled()
			.addAuthPattern(HttpMethod.ALL, "/blade-chat/message/**", "hasAuth()")
			.addAuthPattern(HttpMethod.POST, "/blade-desk/dashboard/upload", "hasTimeAuth(9, 17)")
			.addAuthPattern(HttpMethod.POST, "/blade-desk/dashboard/submit", "hasAnyRole('administrator', 'admin', 'user')")
			.basicEnabled()
			.addBasicPattern(HttpMethod.POST, "/blade-desk/dashboard/info", "blade", "blade")
			.signDisabled()
			.addSignPattern(HttpMethod.POST, "/blade-desk/dashboard/sign", "sha1");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/cors/**")
			.allowedOriginPatterns("*")
			.allowedHeaders("*")
			.allowedMethods("*")
			.maxAge(3600)
			.allowCredentials(true);
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.addPathPrefix(StringPool.SLASH + AppConstant.APPLICATION_AUTH_NAME,
			c -> c.isAnnotationPresent(RestController.class) && (
				OAuth2TokenEndPoint.class.equals(c) || OAuth2SocialEndpoint.class.equals(c) || Oauth2SmsEndpoint.class.equals(c))
		);
	}
}
