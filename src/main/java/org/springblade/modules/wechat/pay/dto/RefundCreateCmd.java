package org.springblade.modules.wechat.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundCreateCmd {

	private String outTradeNo;
	private String outRefundNo;
	private BigDecimal refundAmountYuan;
	private BigDecimal totalAmountYuan;
	private String reason;
	private String notifyUrl;
}

