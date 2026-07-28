package org.springblade.modules.recommend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.recommend.service.IRecommendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 推荐系统接口，个性化身份只以后端登录态为准。 */
@RestController
@AllArgsConstructor
@RequestMapping("/blade-recommend")
@Tag(name = "推荐系统", description = "兴趣、行为反馈和内容时效综合推荐")
public class RecommendController extends BladeController {

	private final IRecommendService recommendService;

	@RequestMapping("/recommendToUserByCF")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "兼容随机推荐", description = "客户端 uid 参数仅为历史兼容，实际使用后端登录身份")
	public R<Map<String, Object>> recommendToUserByCF(@RequestParam long page,
		@RequestParam long limit,
		@RequestParam(required = false) String uid) {
		return R.data(recommendService.recommendToUserByCF(page, limit, currentUserIdText()));
	}

	@RequestMapping("/recommendToUser")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "兼容智能推荐", description = "客户端 uid 参数仅为历史兼容，实际使用后端登录身份")
	public R<Map<String, Object>> recommendToUser(@RequestParam long page,
		@RequestParam long limit,
		@RequestParam(required = false) String uid) {
		return R.data(recommendService.recommendToUser(page, limit, currentUserIdText()));
	}

	@GetMapping("/home-feed")
	@Operation(summary = "首页混合推荐", description = "登录用户按兴趣、浏览和反馈排序；匿名用户使用通用推荐")
	public R<Map<String, Object>> homeFeed(@RequestParam(defaultValue = "1") long page,
		@RequestParam(defaultValue = "20") long limit,
		@RequestParam(required = false) Long uid) {
		return R.data(recommendService.homeFeed(page, limit, currentUserId()));
	}

	private Long currentUserId() {
		Long userId = AuthUtil.getUserId();
		return Func.isEmpty(userId) || userId <= 0 ? null : userId;
	}

	private String currentUserIdText() {
		Long userId = currentUserId();
		return userId == null ? "" : String.valueOf(userId);
	}
}
