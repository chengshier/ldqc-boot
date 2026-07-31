package org.springblade.modules.imgDetail.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springblade.common.constant.PlatformMqConstant;
import org.springblade.common.constant.platform.PlatformConstant;
import org.springblade.common.utils.RedisUtils;
import org.springblade.common.utils.SendMessageMq;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.category.pojo.entity.CategoryEntity;
import org.springblade.modules.category.service.ICategoryService;
import org.springblade.modules.imgDetail.pojo.dto.ImgDetailDTO;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorBizType;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorEventCode;
import org.springblade.modules.pointsbehavior.service.IBehaviorFacade;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.tag.pojo.entity.TagEntity;
import org.springblade.modules.tag.service.ITagService;
import org.springblade.modules.tagimgrelation.pojo.entity.TagImgRelationEntity;
import org.springblade.modules.tagimgrelation.service.ITagImgRelationService;
import org.springblade.modules.talentpost.pojo.entity.TalentPostEntity;
import org.springblade.modules.talentpost.service.ITalentPostService;
import org.springblade.modules.userauthtype.pojo.entity.UserAuthTypeEntity;
import org.springblade.modules.userauthtype.service.IUserAuthTypeService;
import org.springblade.modules.usermessage.pojo.entity.UserMessage;
import org.springblade.modules.usermessage.service.IUserMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 社区图文与短视频发布工作流。
 *
 * <p>底部发布入口只创建社区 UGC。发布者身份由服务端登录态确定，内容先进入
 * 待审核状态；审核通过后才进入公开缓存、达人主页和绿豆奖励链路。</p>
 */
@Service
@RequiredArgsConstructor
public class ContentPublishWorkflowService {

	public static final int STATUS_PENDING = 0;
	public static final int STATUS_PUBLISHED = 1;
	public static final int STATUS_REJECTED = 2;
	public static final int STATUS_OFFLINE = 3;

	private static final int MAX_IMAGE_COUNT = 9;
	private static final int MAX_TAG_COUNT = 5;

	private final IImgDetailService imgDetailService;
	private final ITagService tagService;
	private final ITagImgRelationService tagImgRelationService;
	private final ICategoryService categoryService;
	private final IUserService userService;
	private final ITalentPostService talentPostService;
	private final IUserAuthTypeService userAuthTypeService;
	private final IUserMessageService userMessageService;
	private final IBehaviorFacade behaviorFacade;
	private final RedisUtils redisUtils;
	private final SendMessageMq sendMessageMq;

	@Transactional(rollbackFor = Exception.class)
	public Long submit(ImgDetailDTO request, Long currentUserId) {
		if (currentUserId == null || currentUserId <= 0) {
			throw new ServiceException("请先登录后再发布内容");
		}
		if (request == null) {
			throw new ServiceException("发布内容不能为空");
		}

		String mediaType = normalizeMediaType(request.getMediaType(), request.getMediaUrl(), request.getImgsUrl());
		List<String> imageUrls = parseImageUrls(request.getImgsUrl());
		validateContent(request, mediaType, imageUrls);
		validateCategory(request.getCategoryId(), request.getCategoryPid());
		List<String> tagNames = normalizeTagNames(request.getTags());

		ImgDetailEntity content = BeanUtil.copy(request, ImgDetailEntity.class);
		content.setId(null);
		content.setUserId(currentUserId);
		content.setMediaType(mediaType);
		content.setImgsUrl(imageUrls.isEmpty() ? null : JSON.toJSONString(imageUrls));
		content.setCount(imageUrls.isEmpty() ? 1 : imageUrls.size());
		content.setCollectionCount(0L);
		content.setCommentCount(0L);
		content.setAgreeCount(0L);
		content.setViewCount(0L);
		content.setStatus(STATUS_PENDING);
		content.setAuditReason(null);
		content.setAuditTime(null);
		content.setAuditUserId(null);
		content.setPublishTime(null);
		content.setMediaProcessStatus(needsVideoPoster(content) ? "PROCESSING" : "READY");
		if (!imgDetailService.save(content)) {
			throw new ServiceException("内容保存失败，请重试");
		}

		for (String tagName : tagNames) {
			long tagId = tagService.saveTagByName(tagName);
			TagImgRelationEntity relation = new TagImgRelationEntity();
			relation.setMid(content.getId());
			relation.setTid(tagId);
			tagImgRelationService.save(relation);
		}
		return content.getId();
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteOwned(List<Long> ids, Long currentUserId) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		if (currentUserId == null || currentUserId <= 0) {
			throw new ServiceException("请先登录");
		}
		List<ImgDetailEntity> owned = imgDetailService.list(Wrappers.<ImgDetailEntity>lambdaQuery()
			.in(ImgDetailEntity::getId, ids)
			.eq(ImgDetailEntity::getUserId, currentUserId)
			.eq(ImgDetailEntity::getIsDeleted, 0));
		if (owned.size() != ids.size()) {
			throw new ServiceException("包含不存在或无权删除的内容");
		}
		imgDetailService.deleteImgs(ids, currentUserId);
		for (Long id : ids) {
			removeFromPublicChannels(id);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void audit(Long contentId, String actionValue, String reason, Long auditorUserId) {
		ImgDetailEntity content = imgDetailService.getById(contentId);
		if (content == null || Func.equals(content.getIsDeleted(), 1)) {
			throw new ServiceException("内容不存在");
		}
		String action = Func.toStr(actionValue, "").trim().toUpperCase(Locale.ROOT);
		Date now = new Date();
		if ("PASS".equals(action)) {
			if (content.getStatus() != null && content.getStatus() == STATUS_PUBLISHED) {
				throw new ServiceException("内容已经审核通过");
			}
			if (needsVideoPoster(content)) {
				throw new ServiceException("视频封面仍在处理中，请稍后再审核");
			}
			content.setStatus(STATUS_PUBLISHED);
			content.setAuditReason(null);
			content.setAuditTime(now);
			content.setAuditUserId(auditorUserId);
			content.setPublishTime(now);
			content.setMediaProcessStatus("READY");
			imgDetailService.updateById(content);
			publishToPublicChannels(content);
			createAuditMessage(content, "CONTENT_AUDIT_PASS", "内容审核通过", "你发布的内容已审核通过并公开展示。");
			return;
		}
		if ("REJECT".equals(action)) {
			if (Func.isBlank(reason)) {
				throw new ServiceException("驳回时必须填写原因");
			}
			content.setStatus(STATUS_REJECTED);
			content.setAuditReason(reason.trim());
			content.setAuditTime(now);
			content.setAuditUserId(auditorUserId);
			imgDetailService.updateById(content);
			removeFromPublicChannels(contentId);
			createAuditMessage(content, "CONTENT_AUDIT_REJECT", "内容审核未通过", "审核原因：" + reason.trim());
			return;
		}
		if ("OFFLINE".equals(action)) {
			if (Func.isBlank(reason)) {
				throw new ServiceException("下架时必须填写原因");
			}
			content.setStatus(STATUS_OFFLINE);
			content.setAuditReason(reason.trim());
			content.setAuditTime(now);
			content.setAuditUserId(auditorUserId);
			imgDetailService.updateById(content);
			removeFromPublicChannels(contentId);
			createAuditMessage(content, "CONTENT_OFFLINE", "内容已下架", "下架原因：" + reason.trim());
			return;
		}
		throw new ServiceException("不支持的审核动作");
	}

	public ImgDetailVO getVisibleDetail(String id, Long currentUserId, boolean administrator) {
		ImgDetailEntity content = imgDetailService.getById(id);
		if (content == null || Func.equals(content.getIsDeleted(), 1)) {
			throw new ServiceException("内容不存在");
		}
		boolean owner = currentUserId != null && currentUserId.equals(content.getUserId());
		if (!administrator && !owner && !Integer.valueOf(STATUS_PUBLISHED).equals(content.getStatus())) {
			throw new ServiceException("内容正在审核或已下架");
		}
		return imgDetailService.getImgDetail(id);
	}

	private void publishToPublicChannels(ImgDetailEntity content) {
		redisUtils.hPut(PlatformConstant.IMG_DETAIL_LIST_KEY, String.valueOf(content.getId()), JSON.toJSONString(content));
		User user = userService.getById(content.getUserId());
		if (user != null) {
			sendMessageMq.sendMessage(PlatformMqConstant.USER_STATE_EXCHANGE, PlatformMqConstant.USER_STATE_KEY, user);
			syncTalentPost(content, user);
		}
		Map<String, Object> ext = new HashMap<>();
		ext.put("categoryId", content.getCategoryId());
		ext.put("mediaType", content.getMediaType());
		ext.put("auditUserId", content.getAuditUserId());
		behaviorFacade.onSuccess(BehaviorEventCode.CONTENT_PUBLISH_SUCCESS, BehaviorBizType.IMG_DETAIL,
			String.valueOf(content.getId()), content.getUserId(), null, ext);
	}

	private void removeFromPublicChannels(Long contentId) {
		redisUtils.getRedisTemplate().opsForHash().delete(PlatformConstant.IMG_DETAIL_LIST_KEY, String.valueOf(contentId));
		talentPostService.remove(Wrappers.<TalentPostEntity>lambdaQuery()
			.eq(TalentPostEntity::getSourceContentId, contentId));
	}

	private void syncTalentPost(ImgDetailEntity content, User user) {
		if (!isApprovedTalent(user)) {
			return;
		}
		TalentPostEntity post = talentPostService.getOne(Wrappers.<TalentPostEntity>lambdaQuery()
			.eq(TalentPostEntity::getSourceContentId, content.getId())
			.last("limit 1"));
		if (post == null) {
			post = new TalentPostEntity();
			post.setSourceContentId(content.getId());
		}
		post.setUserId(content.getUserId());
		post.setTitle(resolveTitle(content));
		post.setContent(content.getContent());
		post.setCoverImage(content.getCover());
		post.setPosterUrl(firstNonBlank(content.getPosterUrl(), content.getCover()));
		post.setMediaUrl(resolveMediaUrl(content));
		post.setMediaType(content.getMediaType());
		post.setDuration(content.getDuration());
		post.setFileSize(content.getFileSize());
		post.setWidth(content.getWidth());
		post.setHeight(content.getHeight());
		post.setAgreeCount(Func.toInt(content.getAgreeCount(), 0));
		post.setCommentCount(Func.toInt(content.getCommentCount(), 0));
		post.setShareCount(0);
		post.setViewCount(Func.toInt(content.getViewCount(), 0));
		post.setPostTag(loadFirstTag(content.getId()));
		talentPostService.saveOrUpdate(post);
	}

	private void createAuditMessage(ImgDetailEntity content, String type, String title, String messageContent) {
		UserMessage message = new UserMessage();
		message.setUserId(content.getUserId());
		message.setMessageType(type);
		message.setTitle(title);
		message.setContent(messageContent);
		message.setBizType("IMG_DETAIL");
		message.setBizId(content.getId());
		message.setReadStatus((byte) 0);
		message.setStatus(1);
		userMessageService.save(message);
	}

	private void validateContent(ImgDetailDTO request, String mediaType, List<String> imageUrls) {
		if (Func.isBlank(request.getContent())) {
			throw new ServiceException("请输入内容文案");
		}
		if (request.getContent().trim().length() > 2000) {
			throw new ServiceException("内容文案不能超过2000字");
		}
		if ("VIDEO".equals(mediaType)) {
			if (Func.isBlank(request.getMediaUrl())) {
				throw new ServiceException("请先上传视频");
			}
			if (request.getDuration() != null && request.getDuration() > 600) {
				throw new ServiceException("社区短视频最长10分钟，长视频课程请从达人工作台发布");
			}
		} else {
			if (imageUrls.isEmpty() && Func.isBlank(request.getMediaUrl()) && Func.isBlank(request.getCover())) {
				throw new ServiceException("请至少上传一张图片");
			}
			if (imageUrls.size() > MAX_IMAGE_COUNT) {
				throw new ServiceException("最多上传" + MAX_IMAGE_COUNT + "张图片");
			}
		}
	}

	private void validateCategory(Long categoryId, Long categoryPid) {
		if (categoryId == null) {
			throw new ServiceException("请选择运动分类");
		}
		CategoryEntity category = categoryService.getById(categoryId);
		if (category == null || Func.equals(category.getIsDeleted(), 1)) {
			throw new ServiceException("所选运动分类不存在或已停用");
		}
		if (categoryPid != null && category.getPid() != null && !categoryPid.equals(category.getPid())) {
			throw new ServiceException("运动分类层级不匹配");
		}
	}

	private List<String> normalizeTagNames(List<TagEntity> tags) {
		Set<String> names = new LinkedHashSet<>();
		if (tags != null) {
			for (TagEntity tag : tags) {
				String name = Func.toStr(tag == null ? null : tag.getName(), "").trim();
				if (!name.isEmpty()) {
					if (name.length() > 20) {
						throw new ServiceException("单个标签不能超过20个字符");
					}
					names.add(name);
				}
			}
		}
		if (names.size() > MAX_TAG_COUNT) {
			throw new ServiceException("最多选择" + MAX_TAG_COUNT + "个标签");
		}
		return new ArrayList<>(names);
	}

	private List<String> parseImageUrls(String imgsUrl) {
		if (Func.isBlank(imgsUrl)) {
			return new ArrayList<>();
		}
		try {
			List<String> values = JSON.parseArray(imgsUrl, String.class);
			Set<String> urls = new LinkedHashSet<>();
			for (String value : values) {
				if (Func.isNotBlank(value)) {
					urls.add(value.trim());
				}
			}
			return new ArrayList<>(urls);
		} catch (Exception exception) {
			throw new ServiceException("图片数据格式不正确");
		}
	}

	private String normalizeMediaType(String mediaType, String mediaUrl, String imgsUrl) {
		String type = Func.toStr(mediaType, "").trim().toUpperCase(Locale.ROOT);
		if ("VIDEO".equals(type) || looksLikeVideo(mediaUrl)) {
			return "VIDEO";
		}
		if ("IMAGE".equals(type) || Func.isNotBlank(imgsUrl)) {
			return "IMAGE";
		}
		return "IMAGE";
	}

	private boolean needsVideoPoster(ImgDetailEntity content) {
		return content != null && "VIDEO".equalsIgnoreCase(content.getMediaType())
			&& Func.isNotBlank(content.getMediaUrl())
			&& Func.isBlank(content.getPosterUrl())
			&& Func.isBlank(content.getCover());
	}

	private boolean looksLikeVideo(String url) {
		String value = Func.toStr(url, "").toLowerCase(Locale.ROOT);
		return value.contains(".mp4") || value.contains(".mov") || value.contains(".m3u8")
			|| value.contains(".webm") || value.contains(".m4v");
	}

	private boolean isApprovedTalent(User user) {
		if (user == null || user.getAuthStatus() == null || user.getAuthStatus() != 2) {
			return false;
		}
		List<UserAuthTypeEntity> types = userAuthTypeService.list();
		for (UserAuthTypeEntity type : types) {
			String code = Func.toStr(type.getCode(), "").trim();
			String name = Func.toStr(type.getName(), "").trim();
			if ((Func.isNotBlank(code) && code.equalsIgnoreCase(Func.toStr(user.getMainIdentityCode(), "")))
				|| (Func.isNotBlank(name) && name.equals(Func.toStr(user.getMainIdentityName(), "")))) {
				return "talent".equalsIgnoreCase(code) || name.contains("达人");
			}
		}
		return Func.toStr(user.getMainIdentityCode(), "").equalsIgnoreCase("talent")
			|| Func.toStr(user.getMainIdentityName(), "").contains("达人");
	}

	private String loadFirstTag(Long contentId) {
		TagImgRelationEntity relation = tagImgRelationService.getOne(Wrappers.<TagImgRelationEntity>lambdaQuery()
			.eq(TagImgRelationEntity::getMid, contentId)
			.last("limit 1"));
		TagEntity tag = relation == null ? null : tagService.getById(relation.getTid());
		return tag == null ? "" : Func.toStr(tag.getName(), "");
	}

	private String resolveMediaUrl(ImgDetailEntity content) {
		if (Func.isNotBlank(content.getMediaUrl())) {
			return content.getMediaUrl().trim();
		}
		List<String> images = parseImageUrls(content.getImgsUrl());
		return images.isEmpty() ? Func.toStr(content.getCover(), "") : images.get(0);
	}

	private String resolveTitle(ImgDetailEntity content) {
		String text = Func.toStr(content.getContent(), "").trim();
		return text.length() > 30 ? text.substring(0, 30) : text;
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			if (Func.isNotBlank(value)) {
				return value.trim();
			}
		}
		return "";
	}
}
