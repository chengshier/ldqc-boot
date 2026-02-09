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
package org.springblade.modules.category.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springblade.modules.category.pojo.entity.CategoryEntity;
import org.springblade.modules.category.pojo.vo.CategoryVO;
import org.springblade.modules.category.excel.CategoryExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;


import java.util.List;

/**
 * 分类表 服务类
 *
 * @author BladeX
 * @since 2026-01-27
 */
public interface ICategoryService extends BaseService<CategoryEntity> {
	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param category 查询参数
	 * @return IPage<CategoryVO>
	 */
	IPage<CategoryVO> selectCategoryPage(IPage<CategoryVO> page, CategoryVO category);


	/**
	 * 导出数据
	 *
	 * @param queryWrapper 查询条件
	 * @return List<CategoryExcel>
	 */
	List<CategoryExcel> exportCategory(Wrapper<CategoryEntity> queryWrapper);


	/**
	 * 得到所有分类，返回树形结构
	 *
	 * @return List<CategoryVO>
	 */
	List<CategoryVO> getTreeCategory();

	/**
	 * 通过分类获取所有的图片
	 *
	 * @param page 分页参数
	 * @param id 分类ID
	 * @param type 类型
	 * @return IPage<ImgDetailVO>
	 */
	IPage<ImgDetailVO> getImgListByCategory(IPage<ImgDetailVO> page, String id, Integer type);
}
