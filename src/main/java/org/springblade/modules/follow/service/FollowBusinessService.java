package org.springblade.modules.follow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.follow.pojo.entity.FollowEntity;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.pojo.vo.TrendVO;
import org.springblade.modules.system.service.IUserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录态驱动的关注业务服务。
 *
 * <p>调用方只能提交目标用户ID，关注发起人始终取当前登录用户。关注、取消关注、
 * 关注状态、关注流、粉丝和关注列表均由此服务统一处理。</p>
 */
@Service
@RequiredArgsConstructor
public class FollowBusinessService {

	private final IFollowService followService;
	private final IUserService userService;

	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> follow(Long viewerId, Long targetUserId) {
		validateUsers(viewerId, targetUserId);
		String activeKey = activeKey(viewerId, targetUserId);
		FollowEntity existed = followService.getOne(Wrappers.<FollowEntity>lambdaQuery()
			.eq(FollowEntity::getActiveUniqueKey, activeKey)
			.eq(FollowEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (existed != null) return status(viewerId, targetUserId);

		FollowEntity relation = new FollowEntity();
		relation.setUid(viewerId);
		relation.setFid(targetUserId);
		relation.setActiveUniqueKey(activeKey);
		try {
			followService.save(relation);
		} catch (DuplicateKeyException exception) {
			return status(viewerId, targetUserId);
		}
		updateCounters(viewerId, targetUserId, 1);
		return status(viewerId, targetUserId);
	}

	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> unfollow(Long viewerId, Long targetUserId) {
		validateLogin(viewerId);
		if (targetUserId == null || targetUserId <= 0) throw new ServiceException("缺少目标用户ID");
		FollowEntity relation = followService.getOne(Wrappers.<FollowEntity>lambdaQuery()
			.eq(FollowEntity::getActiveUniqueKey, activeKey(viewerId, targetUserId))
			.eq(FollowEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (relation == null) return status(viewerId, targetUserId);

		boolean changed = followService.update(Wrappers.<FollowEntity>lambdaUpdate()
			.eq(FollowEntity::getId, relation.getId())
			.eq(FollowEntity::getIsDeleted, 0)
			.set(FollowEntity::getActiveUniqueKey, null)
			.set(FollowEntity::getIsDeleted, 1));
		if (changed) updateCounters(viewerId, targetUserId, -1);
		return status(viewerId, targetUserId);
	}

	public Map<String, Object> status(Long viewerId, Long targetUserId) {
		validateLogin(viewerId);
		if (targetUserId == null || targetUserId <= 0) throw new ServiceException("缺少目标用户ID");
		boolean following = viewerId.equals(targetUserId) || followService.count(Wrappers.<FollowEntity>lambdaQuery()
			.eq(FollowEntity::getActiveUniqueKey, activeKey(viewerId, targetUserId))
			.eq(FollowEntity::getIsDeleted, 0)) > 0;
		boolean followedByTarget = !viewerId.equals(targetUserId) && followService.count(Wrappers.<FollowEntity>lambdaQuery()
			.eq(FollowEntity::getActiveUniqueKey, activeKey(targetUserId, viewerId))
			.eq(FollowEntity::getIsDeleted, 0)) > 0;
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("targetUserId", targetUserId);
		result.put("self", viewerId.equals(targetUserId));
		result.put("following", following && !viewerId.equals(targetUserId));
		result.put("followedByTarget", followedByTarget);
		result.put("mutual", following && followedByTarget && !viewerId.equals(targetUserId));
		result.putAll(counts(targetUserId));
		return result;
	}

	public Map<String, Object> counts(Long targetUserId) {
		if (targetUserId == null || targetUserId <= 0) throw new ServiceException("缺少目标用户ID");
		long followingCount = followService.count(Wrappers.<FollowEntity>lambdaQuery()
			.eq(FollowEntity::getUid, targetUserId)
			.eq(FollowEntity::getIsDeleted, 0));
		long fanCount = followService.count(Wrappers.<FollowEntity>lambdaQuery()
			.eq(FollowEntity::getFid, targetUserId)
			.eq(FollowEntity::getIsDeleted, 0));
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("followingCount", followingCount);
		result.put("fanCount", fanCount);
		return result;
	}

	public IPage<Map<String, Object>> connections(long page, long limit, Long targetUserId, String type, Long viewerId) {
		validateLogin(viewerId);
		Long profileUserId = targetUserId == null || targetUserId <= 0 ? viewerId : targetUserId;
		String normalizedType = Func.toStr(type, "FOLLOWING").trim().toUpperCase();
		if (!"FOLLOWING".equals(normalizedType) && !"FOLLOWERS".equals(normalizedType)) {
			throw new ServiceException("列表类型不正确");
		}

		IPage<FollowEntity> relationPage = followService.page(
			new Page<>(Math.max(page, 1), Math.min(Math.max(limit, 1), 50)),
			"FOLLOWERS".equals(normalizedType)
				? Wrappers.<FollowEntity>lambdaQuery().eq(FollowEntity::getFid, profileUserId).eq(FollowEntity::getIsDeleted, 0).orderByDesc(FollowEntity::getCreateTime)
				: Wrappers.<FollowEntity>lambdaQuery().eq(FollowEntity::getUid, profileUserId).eq(FollowEntity::getIsDeleted, 0).orderByDesc(FollowEntity::getCreateTime));

		List<Long> userIds = relationPage.getRecords().stream()
			.map(item -> "FOLLOWERS".equals(normalizedType) ? item.getUid() : item.getFid())
			.filter(java.util.Objects::nonNull)
			.collect(Collectors.toList());
		Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() : userService.listByIds(userIds).stream()
			.collect(Collectors.toMap(User::getId, item -> item, (left, right) -> left));
		Set<Long> viewerFollowingIds = loadFollowingIds(viewerId, userIds);

		List<Map<String, Object>> records = new ArrayList<>();
		for (Long userId : userIds) {
			User user = userMap.get(userId);
			if (user == null) continue;
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("userId", user.getId());
			item.put("username", Func.isBlank(user.getName()) ? user.getAccount() : user.getName());
			item.put("avatar", user.getAvatar());
			item.put("description", user.getDescription());
			item.put("isTalent", Func.equals(user.getIsTalent(), 1));
			item.put("talentTags", user.getTalentTags());
			item.put("following", viewerFollowingIds.contains(user.getId()));
			item.put("self", viewerId.equals(user.getId()));
			records.add(item);
		}
		Page<Map<String, Object>> result = new Page<>(relationPage.getCurrent(), relationPage.getSize(), relationPage.getTotal());
		result.setRecords(records);
		return result;
	}

	public List<TrendVO> feed(long page, long limit, Long viewerId) {
		validateLogin(viewerId);
		return followService.getAllFollowTrends(Math.max(page, 1), Math.min(Math.max(limit, 1), 50), String.valueOf(viewerId));
	}

	private Set<Long> loadFollowingIds(Long viewerId, List<Long> candidates) {
		if (candidates.isEmpty()) return Collections.emptySet();
		return new HashSet<>(followService.list(Wrappers.<FollowEntity>lambdaQuery()
			.eq(FollowEntity::getUid, viewerId)
			.in(FollowEntity::getFid, candidates)
			.eq(FollowEntity::getIsDeleted, 0)).stream().map(FollowEntity::getFid).collect(Collectors.toSet()));
	}

	private void validateUsers(Long viewerId, Long targetUserId) {
		validateLogin(viewerId);
		if (targetUserId == null || targetUserId <= 0) throw new ServiceException("缺少目标用户ID");
		if (viewerId.equals(targetUserId)) throw new ServiceException("不能关注自己");
		User target = userService.getById(targetUserId);
		if (target == null || Func.equals(target.getIsDeleted(), 1)) throw new ServiceException("目标用户不存在");
	}

	private void validateLogin(Long viewerId) {
		if (viewerId == null || viewerId <= 0) throw new ServiceException("请先登录");
	}

	private void updateCounters(Long viewerId, Long targetUserId, int delta) {
		if (delta > 0) {
			userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, viewerId)
				.setSql("follow_count = IFNULL(follow_count,0) + 1"));
			userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, targetUserId)
				.setSql("fan_count = IFNULL(fan_count,0) + 1"));
		} else {
			userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, viewerId)
				.setSql("follow_count = GREATEST(IFNULL(follow_count,0) - 1, 0)"));
			userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, targetUserId)
				.setSql("fan_count = GREATEST(IFNULL(fan_count,0) - 1, 0)"));
		}
	}

	private String activeKey(Long viewerId, Long targetUserId) {
		return viewerId + ":" + targetUserId;
	}
}
