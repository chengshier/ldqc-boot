/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 * <p>
 * Use of this software is governed by the Commercial License Agreement
 * obtained after purchasing a license from BladeX.
 * <p>
 * 1. This software is for development use only under a valid license
 * from BladeX.
 * <p>
 * 2. Redistribution of this software's source code to any third party
 * without a commercial license is strictly prohibited.
 * <p>
 * 3. Licensees may copyright their own code but cannot use segments
 * from this software for such purposes. Copyright of this software
 * remains with BladeX.
 * <p>
 * Using this software signifies agreement to this License, and the software
 * must not be used for illegal purposes.
 * <p>
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY. The author is
 * not liable for any claims arising from secondary or illegal development.
 * <p>
 * Author: Chill Zhuang (bladejava@qq.com)
 */
package org.springblade.modules.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.message.pojo.dto.MessageDTO;
import org.springblade.modules.message.pojo.entity.MessageEntity;
import org.springblade.modules.message.pojo.vo.MessageVO;
import org.springblade.modules.message.excel.MessageExcel;
import org.springblade.modules.message.mapper.MessageMapper;
import org.springblade.modules.message.service.IMessageService;
import org.springblade.modules.messageuserrelation.pojo.entity.MessageUserRelationEntity;
import org.springblade.modules.messageuserrelation.service.IMessageUserRelationService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息表 服务实现类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Service
public class MessageServiceImpl extends BaseServiceImpl<MessageMapper, MessageEntity> implements IMessageService {

	@Autowired
	private IUserService userService;

	@Autowired
	private IMessageUserRelationService messageUserRelationService;

	@Override
	public IPage<MessageVO> selectMessagePage(IPage<MessageVO> page, MessageVO message) {
		return page.setRecords(baseMapper.selectMessagePage(page, message));
	}


	@Override
	public List<MessageExcel> exportMessage(Wrapper<MessageEntity> queryWrapper) {
		List<MessageExcel> messageList = baseMapper.exportMessage(queryWrapper);
		//messageList.forEach(message -> {
		//	message.setTypeName(DictCache.getValue(DictEnum.YES_NO, Message.getType()));
		//});
		return messageList;
	}


	@Override
	public IPage<MessageVO> getChatRecord(IPage<MessageVO> page, String sendUid, String acceptUid) {
		// 查询发送方到接收方的消息
		List<MessageEntity> sendMsgs = this.list(new QueryWrapper<MessageEntity>()
			.eq("send_id", sendUid).eq("accept_id", acceptUid));

		// 查询接收方到发送方的消息
		List<MessageEntity> acceptMsgs = this.list(new QueryWrapper<MessageEntity>()
			.eq("send_id", acceptUid).eq("accept_id", sendUid));

		List<MessageVO> voList = new ArrayList<>();
		User sendUser = userService.getById(sendUid);
		User acceptUser = userService.getById(acceptUid);

		if (sendMsgs != null) {
			for (MessageEntity msg : sendMsgs) {
				MessageVO vo = BeanUtil.copy(msg, MessageVO.class);
				if (sendUser != null) {
					vo.setUsername(sendUser.getName());
					vo.setAvatar(sendUser.getAvatar());
				}
				voList.add(vo);
			}
		}

		if (acceptMsgs != null) {
			for (MessageEntity msg : acceptMsgs) {
				MessageVO vo = BeanUtil.copy(msg, MessageVO.class);
				// 注意：这里原逻辑是 setAcceptId(sendUid).setSendId(acceptUid) 但数据库已经是这样了
				// 这里主要是填充用户信息，对于接收方发来的消息，发送者是 acceptUser
				if (acceptUser != null) {
					vo.setUsername(acceptUser.getName());
					vo.setAvatar(acceptUser.getAvatar());
				}
				voList.add(vo);
			}
		}

		// 按时间排序
		voList.sort((o1, o2) -> {
			String t1 = o1.getTime();
			String t2 = o2.getTime();
			if (t1 == null) return -1;
			if (t2 == null) return 1;
			return t2.compareTo(t1); // 降序
		});

		// 分页处理 (这里是内存分页，因为合并了两个查询)
		int start = (int) ((page.getCurrent() - 1) * page.getSize());
		int end = (int) Math.min(start + page.getSize(), voList.size());
		List<MessageVO> pageList = new ArrayList<>();
		if (start < voList.size()) {
			pageList = voList.subList(start, end);
		}

		return page.setRecords(pageList).setTotal(voList.size());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addChatRecord(MessageDTO messageDTO) {
		// 往数据库里面添加记录
		MessageEntity messageEntity = BeanUtil.copy(messageDTO, MessageEntity.class);
		messageEntity.setTime(String.valueOf(System.currentTimeMillis()));
		this.save(messageEntity);

		// 保证最近的一次聊天记录 (Send -> Accept)
		MessageUserRelationEntity messageUserRelation = messageUserRelationService.getOne(
			new QueryWrapper<MessageUserRelationEntity>()
				.eq("send_id", messageDTO.getSendId())
				.eq("accept_id", messageDTO.getAcceptId())
				.orderByDesc("update_time")
		);

		if (messageUserRelation == null) {
			messageUserRelation = new MessageUserRelationEntity();
			messageUserRelation.setContent(messageEntity.getContent());
			messageUserRelation.setSendId(messageEntity.getSendId());
			messageUserRelation.setAcceptId(messageEntity.getAcceptId());
			messageUserRelation.setCount(1);
			messageUserRelationService.save(messageUserRelation);
		} else {
			messageUserRelation.setContent(messageEntity.getContent());
			messageUserRelation.setCount(messageUserRelation.getCount() + 1);
			// BaseEntity automatically handles update_time, but we might want to force it if needed
			// But BaseServiceImpl/MyBatisPlus handles it usually.
			messageUserRelationService.updateById(messageUserRelation);
		}

		// (Accept -> Send)
		MessageUserRelationEntity messageUserRelation2 = messageUserRelationService.getOne(
			new QueryWrapper<MessageUserRelationEntity>()
				.eq("send_id", messageDTO.getAcceptId())
				.eq("accept_id", messageDTO.getSendId())
				.orderByDesc("update_time")
		);

		if (messageUserRelation2 == null) {
			messageUserRelation2 = new MessageUserRelationEntity();
			messageUserRelation2.setContent(messageEntity.getContent());
			messageUserRelation2.setSendId(messageEntity.getAcceptId());
			messageUserRelation2.setAcceptId(messageEntity.getSendId());
			messageUserRelationService.save(messageUserRelation2);
		} else {
			messageUserRelation2.setContent(messageEntity.getContent());
			messageUserRelationService.updateById(messageUserRelation2);
		}

		// WebSocket notification (TODO: Integrate WebSocket)
		/*
		try {
			List<MessageVO> chatUserList = getChatUserList(String.valueOf(messageUserRelation.getAcceptId()));
			WebSocketServer.sendMessageTo(JSON.toJSONString(chatUserList), String.valueOf(messageUserRelation.getAcceptId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}

	@Override
	public List<MessageVO> getChatUserList(String uid) {
		List<MessageUserRelationEntity> fromUserList = messageUserRelationService.list(
			new QueryWrapper<MessageUserRelationEntity>().eq("accept_id", uid)
		);
		List<MessageVO> messageVoList = new ArrayList<>();

		if (fromUserList == null || fromUserList.isEmpty()) {
			return messageVoList;
		}

		List<Long> sendUids = fromUserList.stream().map(MessageUserRelationEntity::getSendId).collect(Collectors.toList());
		List<User> userList = userService.listByIds(sendUids);
		HashMap<Long, User> userMap = new HashMap<>();

		if (userList != null) {
			userList.forEach(item -> {
				userMap.put(item.getId(), item);
			});
		}

		for (MessageUserRelationEntity model : fromUserList) {
			MessageVO messageVo = BeanUtil.copy(model, MessageVO.class);
			User userEntity = userMap.get(model.getSendId());
			if (userEntity != null) {
				messageVo.setSendId(model.getSendId());
				messageVo.setAcceptId(model.getAcceptId());
				if (model.getUpdateTime() != null) {
					messageVo.setTime(String.valueOf(model.getUpdateTime().getTime()));
				}
				messageVo.setUsername(userEntity.getName()); // user.name in BladeX
				messageVo.setAvatar(userEntity.getAvatar());
			}
			messageVoList.add(messageVo);
		}
		return messageVoList;
	}

	@Override
	public void updateRecordCount(String sendId, String acceptId) {
		MessageUserRelationEntity messageUserRelation = messageUserRelationService.getOne(
			new QueryWrapper<MessageUserRelationEntity>()
				.eq("send_id", sendId)
				.eq("accept_id", acceptId)
		);
		if (messageUserRelation != null) {
			messageUserRelation.setCount(0);
			messageUserRelationService.updateById(messageUserRelation);
		}
	}

	@Override
	public void deleteRecord(String sendId, String acceptId) {
		messageUserRelationService.remove(
			new QueryWrapper<MessageUserRelationEntity>()
				.eq("send_id", sendId)
				.eq("accept_id", acceptId)
		);
	}

}
