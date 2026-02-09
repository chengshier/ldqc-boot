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
package org.springblade.modules.follow.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springblade.modules.follow.pojo.entity.FollowEntity;
import org.springblade.modules.follow.pojo.vo.FollowVO;
import org.springblade.modules.follow.excel.FollowExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.system.pojo.vo.TrendVO;

import java.util.List;

/**
 * 关注表 服务类
 *
 * @author BladeX
 * @since 2026-01-27
 */
public interface IFollowService extends BaseService<FollowEntity> {
	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param follow 查询参数
	 * @return IPage<FollowVO>
	 */
	IPage<FollowVO> selectFollowPage(IPage<FollowVO> page, FollowVO follow);


	/**
	 * 导出数据
	 *
	 * @param queryWrapper 查询条件
	 * @return List<FollowExcel>
	 */
	List<FollowExcel> exportFollow(Wrapper<FollowEntity> queryWrapper);


	/**
	 * 得到当前用户和关注的所有动态
	 *
	 * @param page
	 * @param limit
	 * @param uid
	 * @return
	 */
	List<TrendVO> getAllFollowTrends(long page, long limit, String uid);

	/**
	 * 根据类型获取所有关注和粉丝
	 *
	 * @param page
	 * @param limit
	 * @param uid
	 * @param type 0代表获取所有粉丝，1代表获取所有关注用户
	 * @return
	 */
	IPage<FollowVO> getAllFriend(long page, long limit, String uid, Integer type);

	/**
	 * 关注用户
	 *
	 * @param uid 当前用户ID
	 * @param fid 被关注用户ID
	 */
	void followUser(String uid, String fid);

	/**
	 * 查看是否关注用户
	 *
	 * @param uid
	 * @param fid
	 * @return
	 */
	boolean isFollow(String uid, String fid);

	/**
	 * 删除关注
	 *
	 * @param uid
	 * @param fid
	 */
	void clearFollow(String uid, String fid);

}
