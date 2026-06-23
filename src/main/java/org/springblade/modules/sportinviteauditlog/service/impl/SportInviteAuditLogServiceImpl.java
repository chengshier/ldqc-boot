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
package org.springblade.modules.sportinviteauditlog.service.impl;

import org.springblade.modules.sportinviteauditlog.pojo.entity.SportInviteAuditLogEntity;
import org.springblade.modules.sportinviteauditlog.pojo.vo.SportInviteAuditLogVO;
import org.springblade.modules.sportinviteauditlog.excel.SportInviteAuditLogExcel;
import org.springblade.modules.sportinviteauditlog.mapper.SportInviteAuditLogMapper;
import org.springblade.modules.sportinviteauditlog.service.ISportInviteAuditLogService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import java.util.List;

/**
 * 运动邀约审核日志表 服务实现类
 *
 * @author BladeX
 * @since 2026-05-21
 */
@Service
public class SportInviteAuditLogServiceImpl extends BaseServiceImpl<SportInviteAuditLogMapper, SportInviteAuditLogEntity> implements ISportInviteAuditLogService {

	@Override
	public IPage<SportInviteAuditLogVO> selectSportInviteAuditLogPage(IPage<SportInviteAuditLogVO> page, SportInviteAuditLogVO sportInviteAuditLog) {
		return page.setRecords(baseMapper.selectSportInviteAuditLogPage(page, sportInviteAuditLog));
	}


	@Override
	public List<SportInviteAuditLogExcel> exportSportInviteAuditLog(Wrapper<SportInviteAuditLogEntity> queryWrapper) {
		List<SportInviteAuditLogExcel> sportInviteAuditLogList = baseMapper.exportSportInviteAuditLog(queryWrapper);
		//sportInviteAuditLogList.forEach(sportInviteAuditLog -> {
		//	sportInviteAuditLog.setTypeName(DictCache.getValue(DictEnum.YES_NO, SportInviteAuditLog.getType()));
		//});
		return sportInviteAuditLogList;
	}

}
