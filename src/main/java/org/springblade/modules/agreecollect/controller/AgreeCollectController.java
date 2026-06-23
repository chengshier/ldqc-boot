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
package org.springblade.modules.agreecollect.controller;

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
import org.springblade.modules.agreecollect.pojo.dto.AgreeCollectDTO;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.agreecollect.pojo.entity.AgreeCollectEntity;
import org.springblade.modules.agreecollect.pojo.vo.AgreeCollectVO;
import org.springblade.modules.agreecollect.excel.AgreeCollectExcel;
import org.springblade.modules.agreecollect.wrapper.AgreeCollectWrapper;
import org.springblade.modules.agreecollect.service.IAgreeCollectService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.tool.constant.BladeConstant;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 点赞收藏表 控制器
 *
 * @author BladeX
 * @since 2026-01-27
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-agreecollect/agreeCollect")
@Tag(name = "点赞收藏表", description = "点赞收藏表接口")
public class AgreeCollectController extends BladeController {

	private final IAgreeCollectService agreeCollectService;

	/**
	 * 点赞收藏表 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入agreeCollect")
	public R<AgreeCollectVO> detail(AgreeCollectEntity agreeCollect) {
		AgreeCollectEntity detail = agreeCollectService.getOne(Condition.getQueryWrapper(agreeCollect));
		return R.data(AgreeCollectWrapper.build().entityVO(detail));
	}
	/**
	 * 点赞收藏表 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入agreeCollect")
	public R<IPage<AgreeCollectVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> agreeCollect, Query query) {
		IPage<AgreeCollectEntity> pages = agreeCollectService.page(Condition.getPage(query), Condition.getQueryWrapper(agreeCollect, AgreeCollectEntity.class));
		return R.data(AgreeCollectWrapper.build().pageVO(pages));
	}

	/**
	 * 点赞收藏表 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description  = "传入agreeCollect")
	public R<IPage<AgreeCollectVO>> page(AgreeCollectVO agreeCollect, Query query) {
		IPage<AgreeCollectVO> pages = agreeCollectService.selectAgreeCollectPage(Condition.getPage(query), agreeCollect);
		return R.data(pages);
	}

	/**
	 * 点赞收藏表 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description  = "传入agreeCollect")
	public R save(@Valid @RequestBody AgreeCollectEntity agreeCollect) {
		return R.status(agreeCollectService.save(agreeCollect));
	}

	/**
	 * 点赞收藏表 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description  = "传入agreeCollect")
	public R update(@Valid @RequestBody AgreeCollectEntity agreeCollect) {
		return R.status(agreeCollectService.updateById(agreeCollect));
	}

	/**
	 * 点赞收藏表 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description  = "传入agreeCollect")
	public R submit(@Valid @RequestBody AgreeCollectEntity agreeCollect) {
		return R.status(agreeCollectService.saveOrUpdate(agreeCollect));
	}

	/**
	 * 点赞收藏表 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(agreeCollectService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出数据
	 */
	@IsAdmin
	@GetMapping("/export-agreeCollect")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description  = "传入agreeCollect")
	public void exportAgreeCollect(@Parameter(hidden = true) @RequestParam Map<String, Object> agreeCollect, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<AgreeCollectEntity> queryWrapper = Condition.getQueryWrapper(agreeCollect, AgreeCollectEntity.class);
		//if (!AuthUtil.isAdministrator()) {
		//	queryWrapper.lambda().eq(AgreeCollect::getTenantId, bladeUser.getTenantId());
		//}
		//queryWrapper.lambda().eq(AgreeCollectEntity::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		List<AgreeCollectExcel> list = agreeCollectService.exportAgreeCollect(queryWrapper);
		ExcelUtil.export(response, "点赞收藏表数据" + DateUtil.time(), "点赞收藏表数据表", list, AgreeCollectExcel.class);
	}


	/**
	 * 点赞图片和评论
	 */
	@RequestMapping("/agree")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "点赞图片和评论", description = "传入agreeCollectDTO")
	public R<Void> agree(@RequestBody AgreeCollectDTO agreeCollectDTO) {
		agreeCollectService.agree(agreeCollectDTO);
		return R.status(true);
	}

	/**
	 * 查看是否点赞
	 */
	@RequestMapping("/isAgree")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "查看是否点赞", description = "传入agreeCollectDTO")
	public R<Boolean> isAgree(@RequestBody AgreeCollectDTO agreeCollectDTO) {
		boolean flag = agreeCollectService.isAgree(agreeCollectDTO);
		return R.data(flag);
	}

	/**
	 * 得到当前用户所有的赞和收藏
	 */
	@RequestMapping("/getAllAgreeAndCollection")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "得到当前用户所有的赞和收藏", description = "传入page, limit, uid")
	public R<IPage<AgreeCollectVO>> getAllAgreeAndCollection(Query query, @RequestParam String uid) {
		IPage<AgreeCollectVO> page = Condition.getPage(query);
		return R.data(agreeCollectService.getAllAgreeAndCollection(page, uid));
	}

	/**
	 * 取消点赞
	 */
	@RequestMapping("/cancelAgree")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "取消点赞", description = "传入agreeCollectDTO")
	public R<Void> cancelAgree(@RequestBody AgreeCollectDTO agreeCollectDTO) {
		agreeCollectService.cancelAgree(agreeCollectDTO);
		return R.status(true);
	}

	/**
	 * 得到所有的收藏
	 */
	@RequestMapping("/getAllCollection")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "得到所有的收藏", description = "传入page, limit, uid, type")
	public R<IPage<AgreeCollectVO>> getAllCollection(Query query, @RequestParam String uid, @RequestParam Integer type) {
		IPage<AgreeCollectVO> page = Condition.getPage(query);
		return R.data(agreeCollectService.getAllCollection(page, uid, type));
	}

	/**
	 * 收藏
	 */
	@RequestMapping("/collection")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "收藏", description = "传入agreeCollectDTO")
	public R<Map<String, String>> collection(@RequestBody AgreeCollectDTO agreeCollectDTO) {
		return R.data(agreeCollectService.collection(agreeCollectDTO));
	}

	/**
	 * 查看是否收藏
	 */
	@RequestMapping({"/isCollection", "/isCollectImgToAlbum"})
	@ApiOperationSupport(order = 7)
	@Operation(summary = "查看是否收藏", description = "传入agreeCollectDTO")
	public R<Boolean> isCollection(@RequestBody AgreeCollectDTO agreeCollectDTO) {
		return R.data(agreeCollectService.isCollection(agreeCollectDTO));
	}

	/**
	 * 取消收藏
	 */
	@RequestMapping("/cancelCollection")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "取消收藏", description = "传入agreeCollectDTO")
	public R<Map<String, String>> cancelCollection(@RequestBody AgreeCollectDTO agreeCollectDTO) {
		return R.data(agreeCollectService.cancelCollection(agreeCollectDTO));
	}

}
