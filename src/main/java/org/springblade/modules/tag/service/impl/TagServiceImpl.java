/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 * <p>
 * Use of this software is governed by the Commercial License Agreement
 * obtained after purchasing a license from BladeX.
 * <p>
 * 1. This software is for development use only under a valid license
 * from BladeX.
 * <p>
 * 2. Redistribution of this software's source code to any third party
 * without a commercial license is strictly prohibited.
 * <p>
 * 3. Licensees may copyright their own code but cannot use segments
 * from this software for such purposes. Copyright of this software
 * remains with BladeX.
 * <p>
 * Using this software signifies agreement to this License, and the software
 * must not be used for illegal purposes.
 * <p>
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY. The author is
 * not liable for any claims arising from secondary or illegal development.
 * <p>
 * Author: Chill Zhuang (bladejava@qq.com)
 */
package org.springblade.modules.tag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.tag.pojo.dto.TagDTO;
import org.springblade.modules.tag.pojo.entity.TagEntity;
import org.springblade.modules.tag.pojo.vo.TagVO;
import org.springblade.modules.tag.excel.TagExcel;
import org.springblade.modules.tag.mapper.TagMapper;
import org.springblade.modules.tag.service.ITagService;
import org.springblade.modules.tagimgrelation.pojo.entity.TagImgRelationEntity;
import org.springblade.modules.tagimgrelation.service.ITagImgRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 标签表 服务实现类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Service
public class TagServiceImpl extends BaseServiceImpl<TagMapper, TagEntity> implements ITagService {


	@Autowired
	@Lazy
	private IImgDetailService imgDetailService;

	@Autowired
	@Lazy
	private ITagImgRelationService tagImgRelationService;


	@Autowired
	private IUserService userService;

	@Override
	public List<TagVO> getAllTag() {
		List<TagEntity> list = this.list();
		return BeanUtil.copy(list, TagVO.class);
	}

	@Override
	public IPage<TagVO> selectTagPage(IPage<TagVO> page, TagVO tag) {
		return page.setRecords(baseMapper.selectTagPage(page, tag));
	}


	@Override
	public List<TagExcel> exportTag(Wrapper<TagEntity> queryWrapper) {
		List<TagExcel> tagList = baseMapper.exportTag(queryWrapper);
		//tagList.forEach(tag -> {
		//	tag.setTypeName(DictCache.getValue(DictEnum.YES_NO, Tag.getType()));
		//});
		return tagList;
	}



	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveTag(TagDTO tagDTO) {
		TagEntity tag = this.getOne(new QueryWrapper<TagEntity>().eq("name", tagDTO.getName()));
		if (tag != null) {
			tag.setCount(tag.getCount() + 1);
			this.updateById(tag);
		} else {
			TagEntity tagEntity = BeanUtil.copy(tagDTO, TagEntity.class);
			tagEntity.setCount(1L);
			this.save(tagEntity);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public long saveTagByName(String name) {
		TagEntity tag = this.getOne(new QueryWrapper<TagEntity>().eq("name", name));
		if (tag != null) {
			tag.setCount(tag.getCount() + 1);
			this.updateById(tag);
			return tag.getId();
		} else {
			TagEntity tagEntity = new TagEntity();
			tagEntity.setName(name);
			tagEntity.setCount(1L);
			this.save(tagEntity);
			return tagEntity.getId();
		}
	}

	@Override
	public IPage<ImgDetailVO> getImgListByTag(long page, long limit, String id, Integer type) {
		List<TagImgRelationEntity> tagImgRelationList = tagImgRelationService.list(new QueryWrapper<TagImgRelationEntity>().eq("tid", id));
		List<Long> mids = tagImgRelationList.stream().map(TagImgRelationEntity::getMid).collect(Collectors.toList());

		IPage<ImgDetailEntity> imgDetailPage;
		QueryWrapper<ImgDetailEntity> queryWrapper = new QueryWrapper<ImgDetailEntity>().in("id", mids);
		if (type == 0) {
			queryWrapper.orderByDesc("create_time");
		} else {
			queryWrapper.orderByDesc("agree_count");
		}

		if (mids.isEmpty()) {
			return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit);
		}

		imgDetailPage = imgDetailService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit), queryWrapper);

		List<ImgDetailEntity> imgDetailList = imgDetailPage.getRecords();
		Collection<Long> uids = new HashSet<>();
		imgDetailList.forEach(item -> {
			uids.add(item.getUserId());
		});

		Map<Long, User> userMap = new HashMap<>();
		if (!uids.isEmpty()) {
			List<User> userList = userService.listByIds(uids);
			userList.forEach(item -> {
				userMap.put(item.getId(), item);
			});
		}

		List<ImgDetailVO> res = new ArrayList<>();
		for (ImgDetailEntity model : imgDetailPage.getRecords()) {
			ImgDetailVO imgDetailVo = BeanUtil.copy(model, ImgDetailVO.class);
			if (userMap.containsKey(model.getUserId())) {
				User user = userMap.get(model.getUserId());
				imgDetailVo.setUserId(user.getId());
				imgDetailVo.setUsername(user.getName()); // BladeX User uses name or account? Usually name or account.
				imgDetailVo.setAvatar(user.getAvatar());
			}
			res.add(imgDetailVo);
		}

		IPage<ImgDetailVO> resultPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit);
		resultPage.setRecords(res);
		resultPage.setTotal(imgDetailPage.getTotal());
		return resultPage;
	}

}
