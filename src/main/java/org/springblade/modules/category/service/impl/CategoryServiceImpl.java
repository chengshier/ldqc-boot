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
package org.springblade.modules.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springblade.core.tool.node.ForestNodeMerger;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.category.pojo.entity.CategoryEntity;
import org.springblade.modules.category.pojo.vo.CategoryVO;
import org.springblade.modules.category.excel.CategoryExcel;
import org.springblade.modules.category.mapper.CategoryMapper;
import org.springblade.modules.category.service.ICategoryService;

import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分类表 服务实现类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Service
public class CategoryServiceImpl extends BaseServiceImpl<CategoryMapper, CategoryEntity> implements ICategoryService {

	@Autowired
	@Lazy
	private IImgDetailService imgDetailService;
	@Autowired
	private IUserService userService;

	@Override
	public IPage<CategoryVO> selectCategoryPage(IPage<CategoryVO> page, CategoryVO category) {
		return page.setRecords(baseMapper.selectCategoryPage(page, category));
	}


	@Override
	public List<CategoryExcel> exportCategory(Wrapper<CategoryEntity> queryWrapper) {
		List<CategoryExcel> categoryList = baseMapper.exportCategory(queryWrapper);
		//categoryList.forEach(category -> {
		//	category.setTypeName(DictCache.getValue(DictEnum.YES_NO, Category.getType()));
		//});
		return categoryList;
	}


	@Override
	public List<CategoryVO> getTreeCategory() {
		List<CategoryEntity> list = this.list();
		List<CategoryVO> voList = BeanUtil.copy(list, CategoryVO.class);
		return buildTree(voList);
	}

	private List<CategoryVO> buildTree(List<CategoryVO> nodes) {
		List<CategoryVO> tree = new ArrayList<>();
		for (CategoryVO node : nodes) {
			if (node.getParentId() == null || node.getParentId() == 0L) {
				tree.add(findChildren(node, nodes));
			}
		}
		return tree;
	}

	private CategoryVO findChildren(CategoryVO root, List<CategoryVO> allNodes) {
		for (CategoryVO node : allNodes) {
			if (root.getId().equals(node.getParentId())) {
				root.getChildren().add(findChildren(node, allNodes));
			}
		}
		return root;
	}

	@Override
	public IPage<ImgDetailVO> getImgListByCategory(IPage<ImgDetailVO> page, String id, Integer type) {
		IPage<ImgDetailEntity> imgDetailPage;
		QueryWrapper<ImgDetailEntity> queryWrapper = new QueryWrapper<>();

		if (type != null && type == 1) {
			// 随便看看
			queryWrapper.eq("category_pid", id).orderByDesc("update_time");
		} else if (type != null && type == 2) {
			// 热门
			queryWrapper.eq("category_pid", id).orderByDesc("agree_count").orderByDesc("update_time");
		} else {
			// 二级分类下的所有笔记
			queryWrapper.eq("category_id", id).orderByDesc("agree_count").orderByDesc("update_time");
		}

		// Create a page for entity
		IPage<ImgDetailEntity> entityPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize());
		imgDetailPage = imgDetailService.page(entityPage, queryWrapper);

		List<ImgDetailEntity> records = imgDetailPage.getRecords();
		List<ImgDetailVO> voList = new ArrayList<>();

		if (records != null && !records.isEmpty()) {
			Set<Long> uids = records.stream().map(ImgDetailEntity::getUserId).collect(Collectors.toSet());
			List<User> userList = userService.listByIds(uids);
			Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, u -> u));

			for (ImgDetailEntity entity : records) {
				ImgDetailVO vo = BeanUtil.copy(entity, ImgDetailVO.class);
				User user = userMap.get(entity.getUserId());
				if (user != null) {
					vo.setUsername(user.getName());
					vo.setAvatar(user.getAvatar());
				}
				voList.add(vo);
			}
		}

		return page.setRecords(voList).setTotal(imgDetailPage.getTotal());
	}

}
