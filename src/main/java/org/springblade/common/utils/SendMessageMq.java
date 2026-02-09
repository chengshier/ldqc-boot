package org.springblade.common.utils;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendMessageMq {

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    //发送消息的方法
    //exchange交换机
    //routingKey路由
    //message消息
    public boolean sendMessage(String exchange,String routingKey,Object message) {
        if (rabbitTemplate != null) {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            return true;
        }
        return false;
    }



	/**
	 * 发送用户状态消息
	 * @param message
	 */
	public void sendUserStateMessage(Object message) {
		this.sendMessage(org.springblade.common.constant.PlatformMqConstant.USER_STATE_EXCHANGE,
			org.springblade.common.constant.PlatformMqConstant.USER_STATE_KEY,


			message);
	}
}
