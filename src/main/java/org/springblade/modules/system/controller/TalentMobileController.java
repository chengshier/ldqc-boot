package org.springblade.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.modules.system.service.TalentProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 体育达人公开主页与本人资料工作台接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/blade-system/talent")
@Tag(name = "体育达人", description = "认证达人列表、主页、作品、教程、课程与本人资料维护")
public class TalentMobileController {

	private final TalentProfileService talentProfileService;

	@GetMapping("/mobile/page")
	@Operation(summary = "认证达人列表")
	public R<IPage<Map<String, Object>>> page(@RequestParam(defaultValue = "1") long current,
		@RequestParam(defaultValue = "20") long size,
		@RequestParam(required = false) String keyword) {
		return R.data(talentProfileService.talentPage(current, size, keyword, optionalViewerId()));
	}

	@GetMapping("/mobile/profile")
	@Operation(summary = "达人主页摘要", description = "只返回公开达人资料、统计和当前用户关注状态")
	public R<Map<String, Object>> profile(@RequestParam Long userId) {
		return R.data(talentProfileService.profile(userId, optionalViewerId()));
	}

	@GetMapping("/mobile/content")
	@Operation(summary = "达人内容分页", description = "type：WORKS、TUTORIALS、COURSES")
	public R<IPage<Map<String, Object>>> content(@RequestParam Long userId,
		@RequestParam(defaultValue = "WORKS") String type,
		@RequestParam(defaultValue = "1") long current,
		@RequestParam(defaultValue = "20") long size) {
		return R.data(talentProfileService.contentPage(userId, type, current, size));
	}

	@GetMapping("/mobile/my-profile")
	@Operation(summary = "我的达人资料")
	public R<Map<String, Object>> myProfile() {
		return R.data(talentProfileService.myProfile(AuthUtil.getUserId()));
	}

	@PostMapping("/mobile/update-profile")
	@Operation(summary = "更新我的达人资料", description = "只能修改简介、标签、封面、描述和在线状态")
	public R<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> body) {
		return R.data(talentProfileService.updateMyProfile(AuthUtil.getUserId(), body));
	}

	private Long optionalViewerId() {
		try {
			Long userId = AuthUtil.getUserId();
			return userId != null && userId > 0 ? userId : null;
		} catch (Exception ignored) {
			return null;
		}
	}
}
