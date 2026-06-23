package org.springblade.modules.wechat.mp.service;

import org.springblade.modules.wechat.mp.dto.BatchSendResult;
import org.springblade.modules.wechat.mp.dto.SubscribeMsgCmd;
import org.springblade.modules.wechat.mp.dto.TemplateMsgCmd;
import org.springblade.modules.wechat.mp.dto.WechatSendResult;

import java.util.List;

public interface WechatMpMessageService {

	/**
	 * 发送公众号模板消息。
	 *
	 * @param cmd 模板消息参数
	 * @return 发送结果
	 */
	WechatSendResult sendTemplateMessage(TemplateMsgCmd cmd);

	/**
	 * 发送订阅消息。
	 *
	 * @param cmd 订阅消息参数
	 * @return 发送结果
	 */
	WechatSendResult sendSubscribeMessage(SubscribeMsgCmd cmd);

	/**
	 * 批量发送模板消息。
	 *
	 * @param cmds 模板消息列表
	 * @return 批量发送结果
	 */
	BatchSendResult batchSendTemplate(List<TemplateMsgCmd> cmds);
}
