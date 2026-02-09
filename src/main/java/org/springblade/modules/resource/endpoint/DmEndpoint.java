package org.springblade.modules.resource.endpoint;


import ai.djl.util.RandomUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

import org.springblade.common.constant.platform.PlatformConstant;
import org.springblade.common.utils.RedisUtils;
import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.modules.resource.service.IDmService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * 邮件服务端点
 *
 * @author BladeX
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping(AppConstant.APPLICATION_RESOURCE_NAME + "/dm/endpoint")
@Tag(name = "邮件服务端点", description = "邮件服务端点")
public class DmEndpoint {

    private final IDmService dmService;
    private final RedisUtils redisUtils;

    @PostMapping("/send-validate")
    @Operation(summary = "发送邮件验证码", description = "传入email")
    public R sendValidate(@RequestParam String email) {
        try {
//
//            String content = RandomUtil.randomNumbers(4);
			String content = String.format("%04d", new java.util.Random().nextInt(10000));

			dmService.sendDm(email, content);
            String key = PlatformConstant.AUTH_CODE_PREFIX + email;
            redisUtils.set(key, content, 60 * 5L);
            return R.status(true);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("发送失败: " + e.getMessage());
        }
    }
}
