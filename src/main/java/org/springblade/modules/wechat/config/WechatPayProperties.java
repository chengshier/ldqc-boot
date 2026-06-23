package org.springblade.modules.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayProperties {

	/**
	 * 商户号
	 */
	private String mchId;
	/**
	 * 小程序/公众号 appId
	 */
	private String appId;
	/**
	 * APIv3 密钥（32位）
	 */
	private String apiV3Key;
	/**
	 * 商户 API 证书序列号（微信支付 v3 Authorization 需要）
	 */
	private String merchantSerialNo;
	/**
	 * 商户私钥文件路径（PKCS8 PEM），与 privateKeyPem 二选一
	 */
	private String privateKeyPath;
	/**
	 * 商户私钥内容（PKCS8 PEM），与 privateKeyPath 二选一
	 */
	private String privateKeyPem;
	/**
	 * 商户 API 密钥（用于前端调起支付签名）
	 */
	private String paySignKey;
	/**
	 * 支付回调地址
	 */
	private String notifyUrl;
	/**
	 * 退款回调地址
	 */
	private String refundNotifyUrl;
	/**
	 * 微信支付网关
	 */
	private String baseUrl = "https://api.mch.weixin.qq.com";
}
