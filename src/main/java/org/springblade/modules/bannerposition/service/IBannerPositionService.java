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
package org.springblade.modules.bannerposition.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springblade.modules.bannerposition.pojo.entity.BannerPositionEntity;
import org.springblade.modules.bannerposition.pojo.vo.BannerPositionVO;
import org.springblade.modules.bannerposition.excel.BannerPositionExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import java.util.List;

/**
 * 宣传Banner位置表 服务类
 *
 * @author BladeX
 * @since 2026-07-06
 */
public interface IBannerPositionService extends BaseService<BannerPositionEntity> {
	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param bannerPosition 查询参数
	 * @return IPage<BannerPositionVO>
	 */
	IPage<BannerPositionVO> selectBannerPositionPage(IPage<BannerPositionVO> page, BannerPositionVO bannerPosition);


	/**
	 * 导出数据
	 *
	 * @param queryWrapper 查询条件
	 * @return List<BannerPositionExcel>
	 */
	List<BannerPositionExcel> exportBannerPosition(Wrapper<BannerPositionEntity> queryWrapper);

}
