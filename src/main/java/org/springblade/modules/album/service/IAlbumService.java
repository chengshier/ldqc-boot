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
package org.springblade.modules.album.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springblade.modules.album.pojo.dto.AlbumDTO;
import org.springblade.modules.album.pojo.entity.AlbumEntity;
import org.springblade.modules.album.pojo.vo.AlbumVO;
import org.springblade.modules.album.excel.AlbumExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import java.util.List;

/**
 * 相册表 服务类
 *
 * @author BladeX
 * @since 2026-01-27
 */
public interface IAlbumService extends BaseService<AlbumEntity> {
	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param album 查询参数
	 * @return IPage<AlbumVO>
	 */
	IPage<AlbumVO> selectAlbumPage(IPage<AlbumVO> page, AlbumVO album);


	/**
	 * 导出数据
	 *
	 * @param queryWrapper 查询条件
	 * @return List<AlbumExcel>
	 */
	List<AlbumExcel> exportAlbum(Wrapper<AlbumEntity> queryWrapper);



	/**
	 * 得到当前用户的所有专辑
	 *
	 * @param uid 用户ID
	 * @return List<AlbumVO>
	 */
	List<AlbumVO> getAllAlbum(Long uid);

	/**
	 * 保存专辑
	 *
	 * @param albumDTO 专辑DTO
	 */
	void saveAlbum(AlbumDTO albumDTO);

	/**
	 * 得到专辑信息
	 *
	 * @param id 专辑ID
	 * @return AlbumVO
	 */
	AlbumVO getAlbum(Long id);

	/**
	 * 删除专辑
	 *
	 * @param id 专辑ID
	 * @param uid 用户ID
	 */
	void deleteAlbum(Long id, Long uid);

	/**
	 * 更新专辑
	 *
	 * @param albumDTO 专辑DTO
	 */
	void updateAlbum(AlbumDTO albumDTO);

}
