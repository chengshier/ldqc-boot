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
package org.springblade.modules.outdoor.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.outdoor.service.IOutdoorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 户外活动截止时间定时任务
 *
 * @author BladeX
 * @since 2026-06-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutdoorDeadlineTask {

	private final IOutdoorService outdoorService;

	/**
	 * 每 5 分钟扫描一次已过截止时间的活动，自动结束报名
	 */
	@Scheduled(cron = "0 */5 * * * ?")
	public void closeExpiredOutdoor() {
		int updated = outdoorService.closeExpiredOutdoor();
		if (updated > 0) {
			log.info("Auto closed {} expired outdoor activities.", updated);
		}
	}

}
