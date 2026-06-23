package org.springblade.modules.wechat.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WechatRefundStatusResult {

	private String outRefundNo;
	private String refundId;
	private String status;
	private BigDecimal refundAmountYuan;
}
