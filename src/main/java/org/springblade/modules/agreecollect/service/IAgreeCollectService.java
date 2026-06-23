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
package org.springblade.modules.agreecollect.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springblade.modules.agreecollect.pojo.dto.AgreeCollectDTO;
import org.springblade.modules.agreecollect.pojo.entity.AgreeCollectEntity;
import org.springblade.modules.agreecollect.excel.AgreeCollectExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.agreecollect.pojo.vo.AgreeCollectVO;

import java.util.List;
import java.util.Map;

/**
 * 点赞收藏表 服务类
 *
 * @author BladeX
 * @since 2026-01-27
 */
public interface IAgreeCollectService extends BaseService<AgreeCollectEntity> {
	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param agreeCollect 查询参数
	 * @return IPage<AgreeCollectVO>
	 */
	IPage<AgreeCollectVO> selectAgreeCollectPage(IPage<AgreeCollectVO> page, AgreeCollectVO agreeCollect);


	/**
	 * 导出数据
	 *
	 * @param queryWrapper 查询条件
	 * @return List<AgreeCollectExcel>
	 */
	List<AgreeCollectExcel> exportAgreeCollect(Wrapper<AgreeCollectEntity> queryWrapper);


	/**
	 * 点赞
	 *
	 * @param agreeCollectDTO
	 */
	void agree(AgreeCollectDTO agreeCollectDTO);

	/**
	 * 查看是否点赞
	 *
	 * @param agreeCollectDTO
	 * @return
	 */
	boolean isAgree(AgreeCollectDTO agreeCollectDTO);

	/**
	 * 得到所有的赞和收藏
	 *
	 * @param page
	 * @param limit
	 * @param uid   当前用户id
	 * @return
	 */
	IPage<AgreeCollectVO> getAllAgreeAndCollection(IPage<AgreeCollectVO> page, String uid);

	/**
	 * 取消点赞
	 *
	 * @param agreeCollectDTO
	 */
	void cancelAgree(AgreeCollectDTO agreeCollectDTO);

	/**
	 * 得到所有的收藏
	 *
	 * @param page
	 * @param limit
	 * @param uid
	 * @param type  0代表收藏的图片，1代表收藏的专辑
	 * @return
	 */
	IPage<AgreeCollectVO> getAllCollection(IPage<AgreeCollectVO> page, String uid, Integer type);

	/**
	 * 收藏
	 *
	 * @param agreeCollectDTO
	 * @return
	 */
	Map<String, String> collection(AgreeCollectDTO agreeCollectDTO);

	/**
	 * 查看是否收藏
	 *
	 * @param agreeCollectDTO
	 * @return
	 */
	boolean isCollection(AgreeCollectDTO agreeCollectDTO);

	/**
	 * 取消收藏
	 *
	 * @param agreeCollectDTO
	 * @return
	 */
	Map<String, String> cancelCollection(AgreeCollectDTO agreeCollectDTO);

}
