package org.springblade.modules.wechat.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WechatRefundResult {

	private String outRefundNo;
	private String refundId;
	private String status;
	private BigDecimal refundAmountYuan;
}
