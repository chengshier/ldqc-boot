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
package org.springblade.modules.tag.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;

import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.tag.pojo.dto.TagDTO;
import org.springblade.modules.tag.pojo.entity.TagEntity;
import org.springblade.modules.tag.pojo.vo.TagVO;
import org.springblade.modules.tag.excel.TagExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import java.util.List;

/**
 * 标签表 服务类
 *
 * @author BladeX
 * @since 2026-01-27
 */
public interface ITagService extends BaseService<TagEntity> {
	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param tag 查询参数
	 * @return IPage<TagVO>
	 */
	IPage<TagVO> selectTagPage(IPage<TagVO> page, TagVO tag);


	/**
	 * 导出数据
	 *
	 * @param queryWrapper 查询条件
	 * @return List<TagExcel>
	 */
	List<TagExcel> exportTag(Wrapper<TagEntity> queryWrapper);



	/**
	 * 获取所有标签
	 *
	 * @return
	 */
	List<TagVO> getAllTag();

	/**
	 * 保存标签
	 *
	 * @param tagDTO
	 */
	void saveTag(TagDTO tagDTO);

	/**
	 * 根据名称保存标签
	 *
	 * @param name
	 * @return
	 */
	long saveTagByName(String name);

	/**
	 * 根据标签id获取图片信息
	 *
	 * @param page
	 * @param limit
	 * @param id
	 * @param type
	 * @return
	 */
	IPage<ImgDetailVO> getImgListByTag(long page, long limit, String id, Integer type);

}
