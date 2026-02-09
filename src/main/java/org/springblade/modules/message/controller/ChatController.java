package org.springblade.modules.message.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.common.utils.TokenServerAssistant;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天模块 控制器
 *
 * @author BladeX
 * @since 2026-01-29
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-message/chat")
@Tag(name = "聊天模块", description = "聊天接口")
public class ChatController extends BladeController {

    /**
     * 得到zim的token信息
     */
    @GetMapping("/getZimToken")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "得到zim的token信息", description = "传入userId")
    public R<String> getZimToken(@RequestParam String userId) {
        TokenServerAssistant.TokenInfo tokenInfo = TokenServerAssistant.generateToken04(1562974438, userId, "516253e568dce2b1739b9c4019277309", 300, "");
        return R.data(tokenInfo.data);
    }

}