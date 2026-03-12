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
package org.springblade.modules.newsuseraction.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springblade.modules.newsuseraction.pojo.entity.NewsUserActionEntity;
import org.springblade.modules.newsuseraction.pojo.vo.NewsUserActionVO;
import org.springblade.modules.newsuseraction.excel.NewsUserActionExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import java.util.List;

/**
 * 用户行为表 服务类
 *
 * @author BladeX
 * @since 2026-03-02
 */
public interface INewsUserActionService extends BaseService<NewsUserActionEntity> {
	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param newsUserAction 查询参数
	 * @return IPage<NewsUserActionVO>
	 */
	IPage<NewsUserActionVO> selectNewsUserActionPage(IPage<NewsUserActionVO> page, NewsUserActionVO newsUserAction);


	/**
	 * 导出数据
	 *
	 * @param queryWrapper 查询条件
	 * @return List<NewsUserActionExcel>
	 */
	List<NewsUserActionExcel> exportNewsUserAction(Wrapper<NewsUserActionEntity> queryWrapper);

}
