package org.springblade.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.pojo.vo.FollowVO;
import org.springblade.modules.system.pojo.vo.TrendVO;
import org.springblade.modules.system.pojo.vo.UserRecordVO;
import org.springblade.modules.system.service.IAppUserService;
import org.springblade.modules.system.service.TalentProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * App 用户扩展接口。
 *
 * <p>所有涉及资产、未公开动态、资料修改和消息计数的操作都以登录态为准，
 * 不再信任客户端传入的 uid 或 id。</p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("/blade-system/app-user")
@Tag(name = "App用户扩展", description = "用户公开资料、本人资产和资料维护接口")
public class AppUserController {

	private final IAppUserService appUserService;
	private final TalentProfileService talentProfileService;

	@GetMapping("/getTrendByUser")
	@Operation(summary = "获取用户动态", description = "他人只能查看已发布动态；本人可查看自己的完整状态")
	public R<IPage<TrendVO>> getTrendByUser(@RequestParam long page, @RequestParam long limit,
		@RequestParam String userId, @RequestParam(required = false) Integer type) {
		Long viewerId = optionalUserId();
		boolean own = viewerId != null && String.valueOf(viewerId).equals(userId);
		Integer safeType = own || isAdministrator() ? type : 0;
		return R.data(appUserService.getTrendByUser(new Page<>(page, limit), userId, safeType == null ? 0 : safeType));
	}

	@GetMapping("/searchUser")
	@Operation(summary = "搜索用户", description = "关注状态以后端登录用户为准")
	public R<IPage<FollowVO>> searchUser(@RequestParam long page, @RequestParam long limit,
		@RequestParam String keyword, @RequestParam(required = false) String uid) {
		Long viewerId = requireUserId();
		return R.data(appUserService.searchUser(new Page<>(page, limit), keyword, String.valueOf(viewerId)));
	}

	@GetMapping("/getUserRecord")
	@Operation(summary = "获取我的消息与互动计数")
	public R<UserRecordVO> getUserRecord(@RequestParam(required = false) String uid) {
		return R.data(appUserService.getUserRecord(String.valueOf(requireUserId())));
	}

	@GetMapping("/getUserAssets")
	@Operation(summary = "获取我的绿豆和优惠券资产")
	public R<Map<String, Object>> getUserAssets(@RequestParam(required = false) String uid) {
		return R.data(appUserService.getUserAssets(String.valueOf(requireUserId())));
	}

	@GetMapping("/talentHome")
	@Operation(summary = "兼容：达人主页", description = "使用新的安全达人聚合数据")
	public R<Map<String, Object>> talentHome(@RequestParam long page, @RequestParam long limit, @RequestParam String uid) {
		Long talentId = Func.toLong(uid);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("user", talentProfileService.profile(talentId, optionalUserId()));
		IPage<Map<String, Object>> contents = talentProfileService.contentPage(talentId, "WORKS", page, limit);
		result.put("posts", contents.getRecords());
		result.put("total", contents.getTotal());
		result.put("current", contents.getCurrent());
		result.put("size", contents.getSize());
		return R.data(result);
	}

	@RequestMapping("/getUserInfo")
	@Operation(summary = "获取安全用户资料", description = "不返回密码、手机号、邮箱、角色和组织权限字段")
	public R<User> getUserInfo(@RequestParam String uid) {
		User source = appUserService.getUserInfo(uid);
		return R.data(source == null ? null : safeUser(source));
	}

	@PostMapping("/updateUser")
	@Operation(summary = "更新我的基础资料", description = "用户ID以后端登录态为准，不允许修改认证、达人或权限字段")
	public R<User> updateUser(@RequestBody User request) {
		Long userId = requireUserId();
		User current = appUserService.getUserInfo(String.valueOf(userId));
		if (current == null) throw new ServiceException("用户不存在");
		current.setName(clean(request.getName(), 50));
		current.setAvatar(clean(request.getAvatar(), 1000));
		current.setDescription(clean(request.getDescription(), 300));
		current.setCover(clean(request.getCover(), 1000));
		current.setAddress(clean(request.getAddress(), 300));
		current.setSex(request.getSex());
		current.setBirthday(request.getBirthday());
		User updated = appUserService.updateUser(current);
		return R.data(safeUser(updated));
	}

	@RequestMapping("/searchUserByUsername")
	@Operation(summary = "按昵称精确搜索用户")
	public R<List<FollowVO>> searchUserByUsername(@RequestParam String keyword) {
		requireUserId();
		return R.data(appUserService.searchUserByUsername(keyword));
	}

	@RequestMapping("/clearUserRecord")
	@Operation(summary = "清除我的消息计数")
	public R<Void> clearUserRecord(@RequestParam(required = false) String uid, @RequestParam Integer type) {
		appUserService.clearUserRecord(String.valueOf(requireUserId()), type);
		return R.status(true);
	}

	private User safeUser(User source) {
		User target = new User();
		target.setId(source.getId());
		target.setName(source.getName());
		target.setRealName(null);
		target.setAvatar(source.getAvatar());
		target.setSex(source.getSex());
		target.setDescription(source.getDescription());
		target.setCover(source.getCover());
		target.setAddress(source.getAddress());
		target.setTrendCount(source.getTrendCount());
		target.setFollowCount(source.getFollowCount());
		target.setFanCount(source.getFanCount());
		target.setLikeCount(source.getLikeCount());
		target.setCollectCount(source.getCollectCount());
		target.setIsTalent(source.getIsTalent());
		target.setTalentTags(source.getTalentTags());
		target.setTalentIntro(source.getTalentIntro());
		target.setTalentOnline(source.getTalentOnline());
		target.setMainIdentityName(source.getMainIdentityName());
		target.setIdentityBadges(source.getIdentityBadges());
		return target;
	}

	private Long requireUserId() {
		Long userId = AuthUtil.getUserId();
		if (userId == null || userId <= 0) throw new ServiceException("请先登录");
		return userId;
	}

	private Long optionalUserId() {
		try {
			Long userId = AuthUtil.getUserId();
			return userId != null && userId > 0 ? userId : null;
		} catch (Exception ignored) {
			return null;
		}
	}

	private boolean isAdministrator() {
		try {
			return AuthUtil.isAdministrator();
		} catch (Exception ignored) {
			return false;
		}
	}

	private String clean(String value, int maxLength) {
		String text = value == null ? "" : value.trim();
		return text.length() > maxLength ? text.substring(0, maxLength) : text;
	}
}
