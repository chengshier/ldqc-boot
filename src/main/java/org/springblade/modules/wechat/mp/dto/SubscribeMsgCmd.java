package org.springblade.modules.wechat.mp.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SubscribeMsgCmd {

	private String appId;
	private String toUserOpenId;
	private String templateId;
	private String page;
	private String lang;
	private Map<String, Object> data;
	private String bizType;
	private String bizId;
}
