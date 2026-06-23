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
package org.springblade.modules.sportinvitemedia.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springblade.modules.sportinvitemedia.pojo.entity.SportInviteMediaEntity;
import org.springblade.modules.sportinvitemedia.pojo.vo.SportInviteMediaVO;
import org.springblade.modules.sportinvitemedia.excel.SportInviteMediaExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import java.util.List;

/**
 * 运动邀约媒体表 服务类
 *
 * @author BladeX
 * @since 2026-05-21
 */
public interface ISportInviteMediaService extends BaseService<SportInviteMediaEntity> {
	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param sportInviteMedia 查询参数
	 * @return IPage<SportInviteMediaVO>
	 */
	IPage<SportInviteMediaVO> selectSportInviteMediaPage(IPage<SportInviteMediaVO> page, SportInviteMediaVO sportInviteMedia);


	/**
	 * 导出数据
	 *
	 * @param queryWrapper 查询条件
	 * @return List<SportInviteMediaExcel>
	 */
	List<SportInviteMediaExcel> exportSportInviteMedia(Wrapper<SportInviteMediaEntity> queryWrapper);

}
