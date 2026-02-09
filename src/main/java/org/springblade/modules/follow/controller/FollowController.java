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
package org.springblade.modules.follow.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import lombok.AllArgsConstructor;
import jakarta.validation.Valid;

import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.follow.pojo.dto.FollowDTO;
import org.springblade.modules.system.pojo.vo.TrendVO;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.follow.pojo.entity.FollowEntity;
import org.springblade.modules.follow.pojo.vo.FollowVO;
import org.springblade.modules.follow.excel.FollowExcel;
import org.springblade.modules.follow.wrapper.FollowWrapper;
import org.springblade.modules.follow.service.IFollowService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 关注表 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-follow/follow")
@Tag(name = "关注表", description = "关注表接口")
public class FollowController extends BladeController {

	private final IFollowService followService;

	/**
	 * 关注表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入follow")
	public R<FollowVO> detail(FollowEntity follow) {
		FollowEntity detail = followService.getOne(Condition.getQueryWrapper(follow));
		return R.data(FollowWrapper.build().entityVO(detail));
	}
	/**
	 * 关注表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入follow")
	public R<IPage<FollowVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> follow, Query query) {
		IPage<FollowEntity> pages = followService.page(Condition.getPage(query), Condition.getQueryWrapper(follow, FollowEntity.class));
		return R.data(FollowWrapper.build().pageVO(pages));
	}

	/**
	 * 关注表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入follow")
	public R<IPage<FollowVO>> page(FollowVO follow, Query query) {
		IPage<FollowVO> pages = followService.selectFollowPage(Condition.getPage(query), follow);
		return R.data(pages);
	}

	/**
	 * 关注表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入follow")
	public R save(@Valid @RequestBody FollowEntity follow) {
		return R.status(followService.save(follow));
	}

	/**
	 * 关注表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入follow")
	public R update(@Valid @RequestBody FollowEntity follow) {
		return R.status(followService.updateById(follow));
	}

	/**
	 * 关注表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入follow")
	public R submit(@Valid @RequestBody FollowEntity follow) {
		return R.status(followService.saveOrUpdate(follow));
	}

	/**
	 * 关注表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(followService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-follow")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入follow")
	public void exportFollow(@Parameter(hidden = true) @RequestParam Map<String, Object> follow, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<FollowEntity> queryWrapper = Condition.getQueryWrapper(follow, FollowEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(Follow::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(FollowEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<FollowExcel> list = followService.exportFollow(queryWrapper);
		ExcelUtil.export(response, "关注表数据" + DateUtil.time(), "关注表数据表", list, FollowExcel.class);
	}



	/**
	 * 得到当前用户和关注的所有动态
	 */
	@RequestMapping("/getAllFollowTrends")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "得到当前用户和关注的所有动态", description = "传入page, limit, uid")
	public R<List<TrendVO>> getAllFollowTrends(@RequestParam long page, @RequestParam long limit, @RequestParam String uid) {
		List<TrendVO> list = followService.getAllFollowTrends(page, limit, uid);
		return R.data(list);
	}

	/**
	 * 根据类型获取所有关注和粉丝
	 */
	@RequestMapping("/getAllFriend")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "根据类型获取所有关注和粉丝", description = "传入page, limit, uid, type(0粉丝, 1关注)")
	public R<IPage<FollowVO>> getAllFriend(@RequestParam long page, @RequestParam long limit, @RequestParam String uid, @RequestParam Integer type) {
		IPage<FollowVO> pages = followService.getAllFriend(page, limit, uid, type);
		return R.data(pages);
	}

	/**
	 * 关注用户
	 */
	@PostMapping("/followUser")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "关注用户", description = "传入FollowDTO")
	public R<Void> followUser(@RequestBody FollowDTO followDTO) {
		followService.followUser(followDTO.getUid().toString(), followDTO.getFid().toString());
		return R.status(true);
	}

	/**
	 * 查看是否关注用户
	 */
	@RequestMapping("/isFollow")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "查看是否关注用户", description = "传入uid, fid")
	public R<Boolean> isFollow(@RequestParam String uid, @RequestParam String fid) {
		return R.data(followService.isFollow(uid, fid));
	}

	/**
	 * 删除关注
	 */
	@PostMapping("/clearFollow")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "删除关注", description = "传入FollowDTO")
	public R<Void> clearFollow(@RequestBody FollowDTO followDTO) {
		followService.clearFollow(followDTO.getUid().toString(), followDTO.getFid().toString());
		return R.status(true);
	}

}
