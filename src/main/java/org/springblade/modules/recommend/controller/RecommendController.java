package org.springblade.modules.recommend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springblade.modules.recommend.service.IRecommendService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 推荐系统 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("/blade-recommend")
@Tag(name = "推荐系统", description = "推荐系统接口")
public class RecommendController extends BladeController {

    private final IRecommendService recommendService;

    /**
     * 随机推荐
     */
    @RequestMapping("/recommendToUserByCF")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "随机推荐", description = "传入page, limit, uid")
    public R<Map<String, Object>> recommendToUserByCF(@RequestParam long page, @RequestParam long limit, @RequestParam String uid) {
        return R.data(recommendService.recommendToUserByCF(page, limit, uid));
    }

    /**
     * 智能推荐
     */
    @RequestMapping("/recommendToUser")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "智能推荐", description = "传入page, limit, uid")
    public R<Map<String, Object>> recommendToUser(@RequestParam long page, @RequestParam long limit, @RequestParam String uid) {
        return R.data(recommendService.recommendToUser(page, limit, uid));
    }
}