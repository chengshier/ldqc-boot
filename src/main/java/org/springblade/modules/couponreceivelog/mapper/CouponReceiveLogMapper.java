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
package org.springblade.modules.couponreceivelog.mapper;

import org.springblade.modules.couponreceivelog.pojo.entity.CouponReceiveLogEntity;
import org.springblade.modules.couponreceivelog.pojo.vo.CouponReceiveLogVO;
import org.springblade.modules.couponreceivelog.excel.CouponReceiveLogExcel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户认证类型表 Mapper 接口
 *
 * @author BladeX
 * @since 2026-04-02
 */
public interface CouponReceiveLogMapper extends BaseMapper<CouponReceiveLogEntity> {

	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param couponReceiveLog 查询参数
	 * @return List<CouponReceiveLogVO>
	 */
	List<CouponReceiveLogVO> selectCouponReceiveLogPage(IPage page, CouponReceiveLogVO couponReceiveLog);


	/**
	 * 获取导出数据
	 *
	 * @param queryWrapper 查询条件
	 * @return List<CouponReceiveLogExcel>
	 */
	List<CouponReceiveLogExcel> exportCouponReceiveLog(@Param("ew") Wrapper<CouponReceiveLogEntity> queryWrapper);

}

