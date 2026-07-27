package org.springblade.modules.usermessage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.modules.usermessage.pojo.entity.UserMessage;
import org.springblade.modules.usermessage.service.IUserMessageService;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("blade-usermessage/message")
public class UserMessageController {
	private final IUserMessageService userMessageService;
	@GetMapping("/mobile/page")
	public R<IPage<UserMessage>> mobilePage(@RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "20") Integer size) {
		Long userId = AuthUtil.getUserId();
		return R.data(userMessageService.page(new Page<>(current, size), new LambdaQueryWrapper<UserMessage>()
			.eq(UserMessage::getUserId, userId).eq(UserMessage::getIsDeleted, 0).orderByDesc(UserMessage::getCreateTime)));
	}
	@PostMapping("/mobile/read")
	public R<Boolean> read(@RequestParam Long id) {
		UserMessage message = userMessageService.getById(id);
		if (message == null || !AuthUtil.getUserId().equals(message.getUserId())) return R.fail("消息不存在");
		message.setReadStatus((byte) 1);
		return R.status(userMessageService.updateById(message));
	}
}
