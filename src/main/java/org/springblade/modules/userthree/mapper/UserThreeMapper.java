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
package org.springblade.modules.userthree.mapper;

import org.springblade.modules.userthree.pojo.entity.UserThreeEntity;
import org.springblade.modules.userthree.pojo.vo.UserThreeVO;
import org.springblade.modules.userthree.excel.UserThreeExcel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户微信登录认证表 Mapper 接口
 *
 * @author BladeX
 * @since 2026-02-04
 */
public interface UserThreeMapper extends BaseMapper<UserThreeEntity> {

	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param userThree 查询参数
	 * @return List<UserThreeVO>
	 */
	List<UserThreeVO> selectUserThreePage(IPage page, UserThreeVO userThree);


	/**
	 * 获取导出数据
	 *
	 * @param queryWrapper 查询条件
	 * @return List<UserThreeExcel>
	 */
	List<UserThreeExcel> exportUserThree(@Param("ew") Wrapper<UserThreeEntity> queryWrapper);

}
