package org.springblade.modules.recommend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.RecommendConstant;
import org.springblade.common.constant.platform.PlatformConstant;
import org.springblade.common.utils.RedisUtils;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.agreecollect.pojo.entity.AgreeCollectEntity;
import org.springblade.modules.agreecollect.service.IAgreeCollectService;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.news.pojo.entity.NewsEntity;
import org.springblade.modules.news.service.INewsService;
import org.springblade.modules.recommend.service.IRecommendService;
import org.springblade.modules.recommendfeedback.service.RecommendFeedbackService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.userinterest.pojo.entity.UserInterestEntity;
import org.springblade.modules.userinterest.service.IUserInterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** 推荐服务实现类。 */
@Service
@Slf4j
public class RecommendServiceImpl implements IRecommendService {

	@Autowired
	private RedisUtils redisUtils;
	@Autowired
	private IImgDetailService imgDetailService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IAgreeCollectService agreeCollectService;
	@Autowired
	private INewsService newsService;
	@Autowired
	private IUserInterestService userInterestService;
	@Autowired
	private RecommendFeedbackService recommendFeedbackService;

	@Override
	public Map<String, Object> recommendToUserByCF(long page, long limit, String uid) {
		Map<String, Object> result = new HashMap<>(2);
		String userKey = RecommendConstant.BR_IMG_KEY + uid;
		Long userId = parseUserId(uid);
		Set<Long> hiddenContentIds = recommendFeedbackService.notInterestedContentIds(userId);
		Map<Long, Double> behaviorScores = recommendFeedbackService.contentBehaviorScores(userId);

		List<ImgDetailEntity> browsedRecords = new ArrayList<>();
		if (redisUtils.hasKey(userKey)) {
			List<String> values = redisUtils.lRange(userKey, 0, 19);
			if (values != null) {
				for (String value : values) {
					try {
						ImgDetailVO vo = JSON.parseObject(value, ImgDetailVO.class);
						if (vo != null) browsedRecords.add(BeanUtil.copy(vo, ImgDetailEntity.class));
					} catch (Exception ignored) {
						// 历史缓存异常不阻断推荐。
					}
				}
			}
		}

		Page<ImgDetailEntity> pageParam = new Page<>(1, 500);
		IPage<ImgDetailEntity> databasePage = imgDetailService.page(pageParam,
			new QueryWrapper<ImgDetailEntity>()
				.eq("status", 1)
				.eq("is_deleted", 0)
				.orderByDesc("create_time"));
		List<ImgDetailEntity> candidates = new ArrayList<>(databasePage.getRecords());
		candidates.removeIf(item -> item.getId() != null && hiddenContentIds.contains(item.getId()));
		Collections.shuffle(candidates);
		candidates.sort((left, right) -> Double.compare(
			behaviorScores.getOrDefault(right.getId(), 0D),
			behaviorScores.getOrDefault(left.getId(), 0D)));

		Set<Long> browsedIds = browsedRecords.stream()
			.filter(Objects::nonNull)
			.map(ImgDetailEntity::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		long safePage = Math.max(page, 1);
		long safeLimit = Math.min(Math.max(limit, 1), 50);
		int from = (int) Math.min((safePage - 1) * safeLimit, candidates.size());
		List<ImgDetailEntity> unbrowsed = candidates.stream()
			.filter(item -> item.getId() == null || !browsedIds.contains(item.getId()))
			.collect(Collectors.toList());
		int to = (int) Math.min(from + safeLimit, unbrowsed.size());
		List<ImgDetailEntity> pageRecords = from >= to ? Collections.emptyList() : unbrowsed.subList(from, to);

		result.put(RecommendConstant.RECORDS, populateUserInfo(pageRecords, uid));
		result.put(RecommendConstant.TOTAL, unbrowsed.size());
		return result;
	}

	@Override
	public Map<String, Object> recommendToUser(long page, long limit, String uid) {
		return recommendToUserByCF(page, limit, uid);
	}

	@Override
	public Map<String, Object> homeFeed(long page, long limit, Long userId) {
		long safePage = Math.max(page, 1);
		long safeLimit = Math.min(Math.max(limit, 1), 50);
		Set<Long> interestIds = userId == null ? Collections.emptySet() : userInterestService.listByUserId(userId).stream()
			.map(UserInterestEntity::getCategoryId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Set<Long> browseCategoryIds = recentBrowseCategories(userId);
		Set<Long> hiddenContentIds = recommendFeedbackService.notInterestedContentIds(userId);
		Map<Long, Double> behaviorScores = recommendFeedbackService.contentBehaviorScores(userId);
		List<ImgDetailEntity> contents = imgDetailService.list(new QueryWrapper<ImgDetailEntity>()
			.eq("status", 1)
			.eq("is_deleted", 0)
			.orderByDesc("create_time")
			.last("LIMIT 300"));
		contents.removeIf(item -> item.getId() != null && hiddenContentIds.contains(item.getId()));
		contents.sort((left, right) -> Double.compare(
			contentScore(right, interestIds, browseCategoryIds, behaviorScores),
			contentScore(left, interestIds, browseCategoryIds, behaviorScores)));

		Map<Long, ImgDetailVO> contentVoMap = populateUserInfo(contents, userId == null ? null : String.valueOf(userId)).stream()
			.collect(Collectors.toMap(ImgDetailVO::getId, value -> value, (left, right) -> left));
		List<NewsEntity> news = newsService.list(new QueryWrapper<NewsEntity>()
			.eq("news_status", 1)
			.eq("is_deleted", 0)
			.orderByDesc("is_top")
			.orderByDesc("publish_time")
			.last("LIMIT 100"));

		long requiredSize = Math.min(safePage * safeLimit, 300);
		List<Map<String, Object>> feed = new ArrayList<>();
		int contentIndex = 0;
		int newsIndex = 0;
		int desiredNews = Math.max(1, (int) Math.round(safeLimit * 0.2));
		while (feed.size() < requiredSize && (contentIndex < contents.size() || newsIndex < news.size())) {
			boolean insertNews = newsIndex < news.size()
				&& (feed.size() + 1) % Math.max(1, safeLimit / desiredNews) == 0;
			if (insertNews || contentIndex >= contents.size()) {
				feed.add(newsItem(news.get(newsIndex++)));
			} else {
				ImgDetailVO content = contentVoMap.get(contents.get(contentIndex++).getId());
				if (content != null) feed.add(contentItem(content));
			}
		}
		int from = (int) Math.min((safePage - 1) * safeLimit, feed.size());
		int to = (int) Math.min(from + safeLimit, feed.size());
		Map<String, Object> result = new HashMap<>();
		result.put("records", feed.subList(from, to));
		result.put("total", contents.size() + news.size());
		return result;
	}

	private Set<Long> recentBrowseCategories(Long userId) {
		if (userId == null || !redisUtils.hasKey(RecommendConstant.BR_IMG_KEY + userId)) {
			return Collections.emptySet();
		}
		return redisUtils.lRange(RecommendConstant.BR_IMG_KEY + userId, 0, 19).stream()
			.map(value -> {
				try {
					return JSON.parseObject(value, ImgDetailVO.class);
				} catch (Exception ignored) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.map(ImgDetailVO::getCategoryPid)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	private double contentScore(ImgDetailEntity item, Set<Long> interests, Set<Long> browseCategories,
		Map<Long, Double> behaviorScores) {
		double score = 0;
		Long categoryId = item.getCategoryPid() == null ? item.getCategoryId() : item.getCategoryPid();
		if (interests.contains(categoryId)) score += 60;
		if (browseCategories.contains(categoryId)) score += 25;
		if (item.getId() != null) score += Math.min(behaviorScores.getOrDefault(item.getId(), 0D), 30D);
		if (item.getCreateTime() != null) {
			long ageDays = Math.max(0, (System.currentTimeMillis() - item.getCreateTime().getTime()) / 86400000L);
			score += Math.max(0, 15 - ageDays);
		}
		return score;
	}

	private Map<String, Object> contentItem(ImgDetailVO vo) {
		Map<String, Object> data = new HashMap<>();
		data.put("itemType", "CONTENT");
		data.put("id", "content:" + vo.getId());
		data.put("contentId", vo.getId());
		data.put("content", vo.getContent());
		data.put("cover", vo.getCover());
		data.put("posterUrl", vo.getPosterUrl());
		data.put("mediaType", vo.getMediaType());
		data.put("imgsUrl", vo.getImgsUrl());
		data.put("username", vo.getUsername());
		data.put("avatar", vo.getAvatar());
		data.put("userId", vo.getUserId());
		data.put("agreeCount", vo.getAgreeCount());
		data.put("isAgree", vo.getIsAgree());
		data.put("createTime", vo.getCreateTime());
		return data;
	}

	private Map<String, Object> newsItem(NewsEntity item) {
		Map<String, Object> data = new HashMap<>();
		data.put("itemType", "NEWS");
		data.put("id", "news:" + item.getId());
		data.put("newsId", item.getId());
		data.put("title", item.getTitle());
		data.put("content", item.getAbstracts());
		data.put("cover", item.getCover());
		data.put("username", item.getUsername() == null ? "绿动资讯" : item.getUsername());
		data.put("agreeCount", item.getAgreeCount());
		data.put("publishTime", item.getPublishTime());
		data.put("tag", "资讯");
		return data;
	}

	private List<ImgDetailVO> populateUserInfo(List<ImgDetailEntity> list, String uid) {
		List<ImgDetailVO> voList = BeanUtil.copy(list, ImgDetailVO.class);
		if (voList.isEmpty()) return voList;
		Set<Long> userIds = voList.stream().map(ImgDetailVO::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
		if (userIds.isEmpty()) return voList;
		Map<Long, User> userMap = userService.listByIds(userIds).stream()
			.collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left));

		Set<Long> likedContentIds = new HashSet<>();
		if (uid != null && !uid.trim().isEmpty()) {
			List<Long> contentIds = voList.stream().map(ImgDetailVO::getId).filter(Objects::nonNull).collect(Collectors.toList());
			if (!contentIds.isEmpty()) {
				List<AgreeCollectEntity> likeList = agreeCollectService.list(new QueryWrapper<AgreeCollectEntity>()
					.eq("uid", uid)
					.eq("type", 1)
					.in("agree_collect_id", contentIds));
				for (AgreeCollectEntity like : likeList) {
					if (like != null && like.getAgreeCollectId() != null) likedContentIds.add(like.getAgreeCollectId());
				}
			}
		}

		for (ImgDetailVO vo : voList) {
			User user = userMap.get(vo.getUserId());
			if (user != null) {
				vo.setUsername(user.getName());
				vo.setAvatar(user.getAvatar());
			}
			if (uid != null && !uid.trim().isEmpty() && vo.getId() != null) {
				String agreeKey = PlatformConstant.AGREE_IMG_KEY + vo.getId();
				boolean redisLiked = redisUtils.sIsMember(agreeKey, uid);
				vo.setIsAgree(redisLiked || likedContentIds.contains(vo.getId()));
			} else {
				vo.setIsAgree(Boolean.FALSE);
			}
		}
		return voList;
	}

	private Long parseUserId(String uid) {
		if (uid == null || uid.trim().isEmpty()) return null;
		try {
			long value = Long.parseLong(uid.trim());
			return value > 0 ? value : null;
		} catch (NumberFormatException ignored) {
			return null;
		}
	}
}
