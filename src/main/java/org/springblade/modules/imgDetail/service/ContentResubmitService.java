package org.springblade.modules.imgDetail.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.category.pojo.entity.CategoryEntity;
import org.springblade.modules.category.service.ICategoryService;
import org.springblade.modules.imgDetail.pojo.dto.ImgDetailDTO;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.tag.pojo.entity.TagEntity;
import org.springblade.modules.tag.service.ITagService;
import org.springblade.modules.tagimgrelation.pojo.entity.TagImgRelationEntity;
import org.springblade.modules.tagimgrelation.service.ITagImgRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 审核拒绝或已下架内容的修改重提服务。
 */
@Service
@RequiredArgsConstructor
public class ContentResubmitService {

	private final IImgDetailService imgDetailService;
	private final ICategoryService categoryService;
	private final ITagService tagService;
	private final ITagImgRelationService tagImgRelationService;

	@Transactional(rollbackFor = Exception.class)
	public ImgDetailEntity resubmit(ImgDetailDTO request, Long currentUserId) {
		if (request == null || request.getId() == null) {
			throw new ServiceException("缺少内容ID");
		}
		ImgDetailEntity existing = imgDetailService.getById(request.getId());
		if (existing == null || Func.equals(existing.getIsDeleted(), 1)) {
			throw new ServiceException("内容不存在");
		}
		if (currentUserId == null || !currentUserId.equals(existing.getUserId())) {
			throw new ServiceException("只能修改自己发布的内容");
		}
		if (!Integer.valueOf(ContentPublishWorkflowService.STATUS_REJECTED).equals(existing.getStatus())
			&& !Integer.valueOf(ContentPublishWorkflowService.STATUS_OFFLINE).equals(existing.getStatus())) {
			throw new ServiceException("只有审核拒绝或已下架的内容可以修改后重提");
		}
		if (Func.isBlank(request.getContent())) {
			throw new ServiceException("请输入内容文案");
		}
		if (request.getCategoryId() == null) {
			throw new ServiceException("请选择运动分类");
		}
		CategoryEntity category = categoryService.getById(request.getCategoryId());
		if (category == null || Func.equals(category.getIsDeleted(), 1)) {
			throw new ServiceException("所选运动分类不存在或已停用");
		}

		String mediaType = normalizeMediaType(request);
		List<String> imageUrls = parseImages(request.getImgsUrl());
		if ("VIDEO".equals(mediaType)) {
			if (Func.isBlank(request.getMediaUrl())) {
				throw new ServiceException("请先上传视频");
			}
			if (request.getDuration() != null && request.getDuration() > 600) {
				throw new ServiceException("社区短视频最长10分钟");
			}
		} else if (imageUrls.isEmpty() && Func.isBlank(request.getCover())) {
			throw new ServiceException("请至少上传一张图片");
		}

		existing.setContent(request.getContent().trim());
		existing.setCategoryId(request.getCategoryId());
		existing.setCategoryPid(request.getCategoryPid());
		existing.setMediaType(mediaType);
		existing.setImgsUrl(imageUrls.isEmpty() ? null : JSON.toJSONString(imageUrls));
		existing.setMediaUrl(request.getMediaUrl());
		existing.setPosterUrl(request.getPosterUrl());
		existing.setCover(request.getCover());
		existing.setDuration(request.getDuration());
		existing.setFileSize(request.getFileSize());
		existing.setWidth(request.getWidth());
		existing.setHeight(request.getHeight());
		existing.setCount(imageUrls.isEmpty() ? 1 : imageUrls.size());
		existing.setStatus(ContentPublishWorkflowService.STATUS_PENDING);
		existing.setAuditReason(null);
		existing.setAuditTime(null);
		existing.setAuditUserId(null);
		existing.setPublishTime(null);
		existing.setMediaProcessStatus(needsPoster(existing) ? "PROCESSING" : "READY");
		imgDetailService.updateById(existing);

		tagImgRelationService.remove(Wrappers.<TagImgRelationEntity>lambdaQuery()
			.eq(TagImgRelationEntity::getMid, existing.getId()));
		for (String tagName : normalizeTags(request.getTags())) {
			long tagId = tagService.saveTagByName(tagName);
			TagImgRelationEntity relation = new TagImgRelationEntity();
			relation.setMid(existing.getId());
			relation.setTid(tagId);
			tagImgRelationService.save(relation);
		}
		return existing;
	}

	private String normalizeMediaType(ImgDetailDTO request) {
		String value = Func.toStr(request.getMediaType(), "").toUpperCase(Locale.ROOT);
		if ("VIDEO".equals(value) || looksLikeVideo(request.getMediaUrl())) {
			return "VIDEO";
		}
		return "IMAGE";
	}

	private boolean needsPoster(ImgDetailEntity content) {
		return "VIDEO".equalsIgnoreCase(content.getMediaType())
			&& Func.isBlank(content.getPosterUrl()) && Func.isBlank(content.getCover());
	}

	private boolean looksLikeVideo(String value) {
		String url = Func.toStr(value, "").toLowerCase(Locale.ROOT);
		return url.contains(".mp4") || url.contains(".mov") || url.contains(".m3u8")
			|| url.contains(".m4v") || url.contains(".webm");
	}

	private List<String> parseImages(String value) {
		if (Func.isBlank(value)) {
			return new ArrayList<>();
		}
		try {
			List<String> list = JSON.parseArray(value, String.class);
			Set<String> unique = new LinkedHashSet<>();
			for (String url : list) {
				if (Func.isNotBlank(url)) unique.add(url.trim());
			}
			if (unique.size() > 9) throw new ServiceException("最多上传9张图片");
			return new ArrayList<>(unique);
		} catch (ServiceException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new ServiceException("图片数据格式不正确");
		}
	}

	private List<String> normalizeTags(List<TagEntity> tags) {
		Set<String> names = new LinkedHashSet<>();
		if (tags != null) {
			for (TagEntity tag : tags) {
				String name = Func.toStr(tag == null ? null : tag.getName(), "").trim();
				if (!name.isEmpty()) names.add(name);
			}
		}
		if (names.size() > 5) throw new ServiceException("最多选择5个标签");
		return new ArrayList<>(names);
	}
}
