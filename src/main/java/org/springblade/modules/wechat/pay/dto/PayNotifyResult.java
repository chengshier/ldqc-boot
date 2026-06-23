package org.springblade.modules.wechat.pay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayNotifyResult {

	private String outTradeNo;
	private String transactionId;
	private String tradeState;
	private BigDecimal payerTotalYuan;
	private String openid;
	private LocalDateTime successTime;
	private String rawBody;
}

