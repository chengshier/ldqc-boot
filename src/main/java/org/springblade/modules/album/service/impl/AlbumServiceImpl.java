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
package org.springblade.modules.album.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springblade.core.redis.cache.BladeRedis;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.album.pojo.dto.AlbumDTO;
import org.springblade.modules.album.pojo.entity.AlbumEntity;
import org.springblade.modules.album.pojo.vo.AlbumVO;
import org.springblade.modules.album.excel.AlbumExcel;
import org.springblade.modules.album.mapper.AlbumMapper;
import org.springblade.modules.album.service.IAlbumService;
import org.springblade.modules.albumimgrelation.pojo.entity.AlbumImgRelationEntity;
import org.springblade.modules.albumimgrelation.service.IAlbumImgRelationService;

import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 相册表 服务实现类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Service
public class AlbumServiceImpl extends BaseServiceImpl<AlbumMapper, AlbumEntity> implements IAlbumService {


	@Autowired
	private IUserService userService;
	@Autowired
	@Lazy
	private IAlbumImgRelationService albumImgRelationService;
	@Autowired
	@Lazy
	private IImgDetailService imgDetailService;
	@Autowired
	private BladeRedis bladeRedis;

	// Constants
	private static final String ALBUM_STATE = "album_state:";

	@Override
	public IPage<AlbumVO> selectAlbumPage(IPage<AlbumVO> page, AlbumVO album) {
		return page.setRecords(baseMapper.selectAlbumPage(page, album));
	}


	@Override
	public List<AlbumExcel> exportAlbum(Wrapper<AlbumEntity> queryWrapper) {
		List<AlbumExcel> albumList = baseMapper.exportAlbum(queryWrapper);
		//albumList.forEach(album -> {
		//	album.setTypeName(DictCache.getValue(DictEnum.YES_NO, Album.getType()));
		//});
		return albumList;
	}



	@Override
	public List<AlbumVO> getAllAlbum(Long uid) {
		List<AlbumEntity> list = this.list(new QueryWrapper<AlbumEntity>().eq("uid", uid).orderByDesc("update_time"));
		return BeanUtil.copy(list, AlbumVO.class);
	}

	@Override
	public AlbumVO getAlbum(Long id) {
		AlbumEntity album = this.getById(id);
		if (album == null) return null;
		AlbumVO albumVo = BeanUtil.copy(album, AlbumVO.class);
		User user = userService.getById(album.getUid());
		if (user != null) {
			albumVo.setUsername(user.getName());
			albumVo.setAvatar(user.getAvatar());
		}
		return albumVo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteAlbum(Long id, Long uid) {
		List<AlbumImgRelationEntity> relations = albumImgRelationService.list(new QueryWrapper<AlbumImgRelationEntity>().eq("aid", id));
		if (relations != null && !relations.isEmpty()) {
			List<Long> idList = relations.stream().map(AlbumImgRelationEntity::getMid).collect(Collectors.toList());
			// TODO: Implement deleteImgs in IImgDetailService
			// imgDetailService.deleteImgs(idList, uid);
			bladeRedis.del(ALBUM_STATE + id);
		}
		this.removeById(id);
	}

	@Override
	public void saveAlbum(AlbumDTO albumDTO) {
		AlbumEntity entity = BeanUtil.copy(albumDTO, AlbumEntity.class);
		this.save(entity);
	}

	@Override
	public void updateAlbum(AlbumDTO albumDTO) {
		AlbumEntity entity = BeanUtil.copy(albumDTO, AlbumEntity.class);
		this.updateById(entity);
	}

}
