package org.springblade.modules.wechat.mp.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TemplateMsgCmd {

	private String appId;
	private String toUserOpenId;
	private String templateId;
	private String pagePath;
	private String url;
	private Map<String, Object> data;
	private String bizType;
	private String bizId;
}
