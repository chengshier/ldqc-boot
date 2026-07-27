package org.springblade.modules.usermessage.service;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.usermessage.mapper.UserMessageMapper;
import org.springblade.modules.usermessage.pojo.entity.UserMessage;
import org.springframework.stereotype.Service;

@Service
public class UserMessageServiceImpl extends BaseServiceImpl<UserMessageMapper, UserMessage> implements IUserMessageService {
}
