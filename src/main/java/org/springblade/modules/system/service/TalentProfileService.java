package org.springblade.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.follow.service.FollowBusinessService;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.talentpost.pojo.entity.TalentPostEntity;
import org.springblade.modules.talentpost.service.ITalentPostService;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.training.service.ITrainingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 体育达人公开主页、作品、教程、课程和本人资料维护服务。
 */
@Service
@RequiredArgsConstructor
public class TalentProfileService {

	private final IUserService userService;
	private final IImgDetailService imgDetailService;
	private final ITalentPostService talentPostService;
	private final ITrainingService trainingService;
	private final FollowBusinessService followBusinessService;

	public IPage<Map<String, Object>> talentPage(long page, long limit, String keyword, Long viewerId) {
		QueryWrapper<User> query = new QueryWrapper<User>()
			.eq("is_talent", 1)
			.eq("auth_status", 2)
			.eq("status", 1)
			.eq("is_deleted", 0);
		if (!Func.isBlank(keyword)) {
			String cleanKeyword = keyword.trim();
			query.and(wrapper -> wrapper.like("name", cleanKeyword)
				.or().like("talent_tags", cleanKeyword)
				.or().like("talent_intro", cleanKeyword));
		}
		query.orderByDesc("talent_sort").orderByDesc("fan_count").orderByDesc("update_time");
		IPage<User> source = userService.page(new Page<>(Math.max(page, 1), Math.min(Math.max(limit, 1), 50)), query);
		List<Map<String, Object>> records = new ArrayList<>();
		for (User user : source.getRecords()) records.add(buildTalentCard(user, viewerId));
		Page<Map<String, Object>> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
		result.setRecords(records);
		return result;
	}

	public Map<String, Object> profile(Long talentUserId, Long viewerId) {
		User talent = requireTalent(talentUserId);
		Map<String, Object> result = buildSafeProfile(talent);
		result.putAll(followBusinessService.counts(talentUserId));
		result.put("workCount", imgDetailService.count(Wrappers.<ImgDetailEntity>lambdaQuery()
			.eq(ImgDetailEntity::getUserId, talentUserId)
			.eq(ImgDetailEntity::getStatus, 1)
			.eq(ImgDetailEntity::getIsDeleted, 0)));
		result.put("tutorialCount", talentPostService.count(Wrappers.<TalentPostEntity>lambdaQuery()
			.eq(TalentPostEntity::getUserId, talentUserId)
			.eq(TalentPostEntity::getStatus, 1)
			.eq(TalentPostEntity::getIsDeleted, 0)));
		result.put("courseCount", trainingService.count(Wrappers.<TrainingEntity>lambdaQuery()
			.eq(TrainingEntity::getTalentUserId, talentUserId)
			.eq(TrainingEntity::getPublishStatus, "PUBLISHED")
			.eq(TrainingEntity::getStatus, 1)
			.eq(TrainingEntity::getIsDeleted, 0)));
		if (viewerId != null && viewerId > 0) {
			result.putAll(followBusinessService.status(viewerId, talentUserId));
		} else {
			result.put("self", false);
			result.put("following", false);
			result.put("followedByTarget", false);
			result.put("mutual", false);
		}
		return result;
	}

	public IPage<Map<String, Object>> contentPage(Long talentUserId, String type, long page, long limit) {
		requireTalent(talentUserId);
		String normalizedType = Func.toStr(type, "WORKS").trim().toUpperCase(Locale.ROOT);
		if ("WORKS".equals(normalizedType)) return workPage(talentUserId, page, limit);
		if ("TUTORIALS".equals(normalizedType)) return tutorialPage(talentUserId, page, limit);
		if ("COURSES".equals(normalizedType)) return coursePage(talentUserId, page, limit);
		throw new ServiceException("达人内容类型不正确");
	}

	public Map<String, Object> myProfile(Long viewerId) {
		return profile(viewerId, viewerId);
	}

	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> updateMyProfile(Long viewerId, Map<String, Object> body) {
		User talent = requireTalent(viewerId);
		String intro = clean(body.get("talentIntro"), 300);
		String tags = normalizeTags(body.get("talentTags"));
		String cover = clean(body.get("cover"), 1000);
		String description = clean(body.get("description"), 300);
		talent.setTalentIntro(intro);
		talent.setTalentTags(tags);
		talent.setCover(cover);
		talent.setDescription(description);
		talent.setTalentOnline(Func.toInt(body.get("talentOnline"), 1) == 1 ? 1 : 0);
		userService.updateById(talent);
		return profile(viewerId, viewerId);
	}

	private IPage<Map<String, Object>> workPage(Long userId, long page, long limit) {
		IPage<ImgDetailEntity> source = imgDetailService.page(new Page<>(safePage(page), safeLimit(limit)),
			Wrappers.<ImgDetailEntity>lambdaQuery()
				.eq(ImgDetailEntity::getUserId, userId)
				.eq(ImgDetailEntity::getStatus, 1)
				.eq(ImgDetailEntity::getIsDeleted, 0)
				.orderByDesc(ImgDetailEntity::getPublishTime)
				.orderByDesc(ImgDetailEntity::getCreateTime));
		List<Map<String, Object>> records = source.getRecords().stream().map(item -> {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("id", item.getId());
			map.put("contentType", "WORK");
			map.put("content", item.getContent());
			map.put("cover", firstNonBlank(item.getPosterUrl(), item.getCover(), firstImage(item.getImgsUrl())));
			map.put("mediaType", item.getMediaType());
			map.put("mediaUrl", item.getMediaUrl());
			map.put("imgsUrl", item.getImgsUrl());
			map.put("duration", item.getDuration());
			map.put("agreeCount", zero(item.getAgreeCount()));
			map.put("commentCount", zero(item.getCommentCount()));
			map.put("viewCount", zero(item.getViewCount()));
			map.put("publishTime", item.getPublishTime());
			return map;
		}).collect(Collectors.toList());
		return mapPage(source, records);
	}

	private IPage<Map<String, Object>> tutorialPage(Long userId, long page, long limit) {
		IPage<TalentPostEntity> source = talentPostService.page(new Page<>(safePage(page), safeLimit(limit)),
			Wrappers.<TalentPostEntity>lambdaQuery()
				.eq(TalentPostEntity::getUserId, userId)
				.eq(TalentPostEntity::getStatus, 1)
				.eq(TalentPostEntity::getIsDeleted, 0)
				.orderByDesc(TalentPostEntity::getCreateTime));
		List<Map<String, Object>> records = source.getRecords().stream().map(item -> {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("id", item.getId());
			map.put("contentType", "TUTORIAL");
			map.put("title", item.getTitle());
			map.put("content", item.getContent());
			map.put("cover", firstNonBlank(item.getPosterUrl(), item.getCoverImage()));
			map.put("mediaType", item.getMediaType());
			map.put("mediaUrl", item.getMediaUrl());
			map.put("duration", item.getDuration());
			map.put("tag", item.getPostTag());
			map.put("agreeCount", zero(item.getAgreeCount()));
			map.put("commentCount", zero(item.getCommentCount()));
			map.put("viewCount", zero(item.getViewCount()));
			map.put("publishTime", item.getCreateTime());
			return map;
		}).collect(Collectors.toList());
		return mapPage(source, records);
	}

	private IPage<Map<String, Object>> coursePage(Long userId, long page, long limit) {
		IPage<TrainingEntity> source = trainingService.page(new Page<>(safePage(page), safeLimit(limit)),
			Wrappers.<TrainingEntity>lambdaQuery()
				.eq(TrainingEntity::getTalentUserId, userId)
				.eq(TrainingEntity::getPublishStatus, "PUBLISHED")
				.eq(TrainingEntity::getStatus, 1)
				.eq(TrainingEntity::getIsDeleted, 0)
				.orderByDesc(TrainingEntity::getUpdateTime));
		List<Map<String, Object>> records = source.getRecords().stream().map(item -> {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("id", item.getId());
			map.put("contentType", "COURSE");
			map.put("title", item.getTitle());
			map.put("cover", item.getCoverImage());
			map.put("description", item.getDescription());
			map.put("instructorName", item.getInstructorName());
			map.put("category", item.getCategory());
			map.put("courseType", item.getCourseType());
			map.put("contentMode", item.getContentMode());
			map.put("price", item.getPrice());
			map.put("totalLessons", item.getTotalLessons());
			map.put("totalVideoDuration", item.getTotalVideoDuration());
			return map;
		}).collect(Collectors.toList());
		return mapPage(source, records);
	}

	private Map<String, Object> buildTalentCard(User user, Long viewerId) {
		Map<String, Object> result = buildSafeProfile(user);
		if (viewerId != null && viewerId > 0 && !viewerId.equals(user.getId())) {
			result.put("following", Boolean.TRUE.equals(followBusinessService.status(viewerId, user.getId()).get("following")));
		} else {
			result.put("following", false);
		}
		return result;
	}

	private Map<String, Object> buildSafeProfile(User user) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", user.getId());
		result.put("name", Func.isBlank(user.getName()) ? "体育达人" : user.getName());
		result.put("avatar", user.getAvatar());
		result.put("cover", user.getCover());
		result.put("intro", Func.isBlank(user.getTalentIntro()) ? user.getDescription() : user.getTalentIntro());
		result.put("tags", splitTags(user.getTalentTags()));
		result.put("talentOnline", Func.equals(user.getTalentOnline(), 1));
		result.put("identityBadges", splitTags(user.getIdentityBadges()));
		result.put("mainIdentityName", user.getMainIdentityName());
		return result;
	}

	private User requireTalent(Long talentUserId) {
		if (talentUserId == null || talentUserId <= 0) throw new ServiceException("缺少达人用户ID");
		User user = userService.getById(talentUserId);
		if (user == null || Func.equals(user.getIsDeleted(), 1)) throw new ServiceException("达人不存在");
		if (!Func.equals(user.getIsTalent(), 1) || !Func.equals(user.getAuthStatus(), 2) || !Func.equals(user.getStatus(), 1)) {
			throw new ServiceException("该用户尚未通过达人认证或已停用");
		}
		return user;
	}

	private String normalizeTags(Object value) {
		List<String> tags = splitTags(value == null ? "" : String.valueOf(value)).stream()
			.map(item -> clean(item, 20)).filter(item -> !Func.isBlank(item)).distinct().limit(8).collect(Collectors.toList());
		return String.join(",", tags);
	}

	private List<String> splitTags(String value) {
		if (Func.isBlank(value)) return Collections.emptyList();
		return Arrays.stream(value.split("[,，]"))
			.map(String::trim).filter(item -> !item.isEmpty()).distinct().limit(12).collect(Collectors.toList());
	}

	private String firstImage(String json) {
		if (Func.isBlank(json)) return "";
		String clean = json.trim();
		if (clean.startsWith("[")) {
			try {
				List<String> images = com.alibaba.fastjson.JSON.parseArray(clean, String.class);
				return images.isEmpty() ? "" : images.get(0);
			} catch (Exception ignored) {
				return "";
			}
		}
		return clean.contains(",") ? clean.split(",")[0] : clean;
	}

	private String firstNonBlank(String... values) {
		for (String value : values) if (!Func.isBlank(value)) return value;
		return "";
	}

	private long safePage(long page) { return Math.max(page, 1); }
	private long safeLimit(long limit) { return Math.min(Math.max(limit, 1), 50); }
	private long zero(Number value) { return value == null ? 0L : value.longValue(); }
	private String clean(Object value, int maxLength) {
		String text = value == null ? "" : String.valueOf(value).trim();
		return text.length() > maxLength ? text.substring(0, maxLength) : text;
	}

	private <T> IPage<Map<String, Object>> mapPage(IPage<T> source, List<Map<String, Object>> records) {
		Page<Map<String, Object>> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
		result.setRecords(records);
		return result;
	}
}
