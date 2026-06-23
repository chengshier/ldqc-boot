package org.springblade.modules.wechat.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayOrderCreateCmd {

	private String appId;
	private String mchId;
	private String outTradeNo;
	private String description;
	private BigDecimal totalAmountYuan;
	private String openid;
	private String notifyUrl;
	private String attach;
	private String clientIp;
	private Integer expireMinutes;
}

