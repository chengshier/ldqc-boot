package org.springblade.modules.wechat.mp.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchSendResult {

	private int total;
	private int successCount;
	private int failCount;
	private List<WechatSendResult> details;
}
