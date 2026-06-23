package org.springblade.modules.wechat.mp.dto;

import lombok.Data;

@Data
public class WechatSendResult {

	private boolean success;
	private String msgId;
	private String errCode;
	private String errMsg;
	private String bizType;
	private String bizId;
}
