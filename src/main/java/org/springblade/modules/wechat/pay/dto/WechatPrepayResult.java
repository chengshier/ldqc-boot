package org.springblade.modules.wechat.pay.dto;

import lombok.Data;

@Data
public class WechatPrepayResult {

	private String prepayId;
	private String appId;
	private String timeStamp;
	private String nonceStr;
	private String pkg;
	private String signType;
	private String paySign;
}

