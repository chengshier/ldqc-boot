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
package org.springblade.modules.albumimgrelation.service.impl;

import org.springblade.modules.albumimgrelation.pojo.entity.AlbumImgRelationEntity;
import org.springblade.modules.albumimgrelation.pojo.vo.AlbumImgRelationVO;
import org.springblade.modules.albumimgrelation.excel.AlbumImgRelationExcel;
import org.springblade.modules.albumimgrelation.mapper.AlbumImgRelationMapper;
import org.springblade.modules.albumimgrelation.service.IAlbumImgRelationService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 相册图片关系表 服务实现类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Service
public class AlbumImgRelationServiceImpl extends BaseServiceImpl<AlbumImgRelationMapper, AlbumImgRelationEntity> implements IAlbumImgRelationService {

	@Override
	public IPage<AlbumImgRelationVO> selectAlbumImgRelationPage(IPage<AlbumImgRelationVO> page, AlbumImgRelationVO albumImgRelation) {
		return page.setRecords(baseMapper.selectAlbumImgRelationPage(page, albumImgRelation));
	}


	@Override
	public List<AlbumImgRelationExcel> exportAlbumImgRelation(Wrapper<AlbumImgRelationEntity> queryWrapper) {
		List<AlbumImgRelationExcel> albumImgRelationList = baseMapper.exportAlbumImgRelation(queryWrapper);
		//albumImgRelationList.forEach(albumImgRelation -> {
		//	albumImgRelation.setTypeName(DictCache.getValue(DictEnum.YES_NO, AlbumImgRelation.getType()));
		//});
		return albumImgRelationList;
	}

}
