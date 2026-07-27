package org.springblade.modules.userinterest.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.modules.category.pojo.entity.CategoryEntity;
import org.springblade.modules.category.service.ICategoryService;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorBizType;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorEventCode;
import org.springblade.modules.pointsbehavior.service.IBehaviorFacade;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.userinterest.pojo.entity.UserInterestEntity;
import org.springblade.modules.userinterest.service.IUserInterestService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/blade-system/user-interest")
@Tag(name = "用户运动爱好", description = "小程序端运动爱好选择与提醒设置")
public class UserInterestController {
	private final IUserInterestService userInterestService;
	private final ICategoryService categoryService;
	private final IUserService userService;
	private final IBehaviorFacade behaviorFacade;

	@GetMapping("/mine")
	@Operation(summary = "获取我的运动爱好")
	public R<List<Long>> mine() {
		Long userId = AuthUtil.getUserId();
		return R.data(userInterestService.listByUserId(userId).stream()
			.map(UserInterestEntity::getCategoryId).collect(Collectors.toList()));
	}

	@PostMapping("/mine")
	@Operation(summary = "保存我的运动爱好")
	@Transactional(rollbackFor = Exception.class)
	public R<List<Long>> saveMine(@RequestBody(required = false) List<Long> categoryIds) {
		Long userId = AuthUtil.getUserId();
		Set<Long> uniqueIds = new LinkedHashSet<>(categoryIds == null ? Collections.emptyList() : categoryIds);
		if (uniqueIds.contains(null) || uniqueIds.size() > 3) {
			return R.fail("最多选择 3 个运动爱好");
		}
		List<Long> ids = uniqueIds.stream().collect(Collectors.toList());
		if (!ids.isEmpty()) {
			List<CategoryEntity> categories = categoryService.list(Wrappers.<CategoryEntity>lambdaQuery()
				.in(CategoryEntity::getId, ids).eq(CategoryEntity::getPid, 0L)
				.eq(CategoryEntity::getIsDeleted, 0));
			if (categories.size() != ids.size()) {
				return R.fail("存在无效的运动分类");
			}
		}
		User user = userService.getById(userId);
		boolean firstCompleted = user.getInterestCompletedAt() == null && !ids.isEmpty();
		userInterestService.replaceForUser(userId, ids);
		if (firstCompleted) {
			user.setInterestCompletedAt(new Date());
			user.setInterestRemindDisabled(1);
			userService.updateById(user);
			behaviorFacade.onSuccess(BehaviorEventCode.PROFILE_INTEREST_COMPLETED, BehaviorBizType.USER_PROFILE,
				String.valueOf(userId), userId, "pi:" + userId, Collections.emptyMap());
		}
		return R.data(ids);
	}

	@PostMapping("/reminder")
	@Operation(summary = "设置运动爱好提醒")
	public R<Void> updateReminder(@RequestParam boolean disabled) {
		User user = new User();
		user.setId(AuthUtil.getUserId());
		user.setInterestRemindDisabled(disabled ? 1 : 0);
		userService.updateById(user);
		return R.success("设置成功");
	}
}
