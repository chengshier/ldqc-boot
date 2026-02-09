package org.springblade.modules.resource.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.platform.PlatformConstant;
import org.springblade.common.utils.RedisUtils;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;

import org.springblade.modules.auth.dto.AuthUserDTO;
import org.springblade.modules.resource.service.IDmService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/util/dm")
@Tag(name = "邮件发送", description = "邮件发送接口")
public class DmController extends BladeController {

    private final IDmService dmService;
    private final RedisUtils redisUtils;

    @PostMapping("/sendDm")
    @Operation(summary = "发送邮件", description = "发送邮件验证码")
    public R sendDm(@RequestBody AuthUserDTO authUserDTO) {
        try {
//            String content = RandomUtil.randomNumbers(4);
			String content = String.format("%04d", new java.util.Random().nextInt(10000));
			dmService.sendDm(authUserDTO.getEmail(), content);
            String key = PlatformConstant.AUTH_CODE_PREFIX + authUserDTO.getEmail();
            redisUtils.set(key, content, 60 * 5L);
            return R.status(true);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("发送失败: " + e.getMessage());
        }
    }
}
