package org.springblade.modules.wechat.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WechatOrderStatusResult {

	private String outTradeNo;
	private String transactionId;
	private String tradeState;
	private BigDecimal totalAmountYuan;
	private BigDecimal payerTotalYuan;
}
