package org.springblade.modules.talentpost.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.talentpost.excel.TalentPostExcel;
import org.springblade.modules.talentpost.pojo.entity.TalentPostEntity;
import org.springblade.modules.talentpost.pojo.vo.TalentPostVO;
import org.springblade.modules.talentpost.service.ITalentPostService;
import org.springblade.modules.talentpost.wrapper.TalentPostWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 达人教程公开展示和管理端维护接口。 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-talentpost/talentPost")
@Tag(name = "达人教程", description = "达人教程公开详情与管理端维护接口")
public class TalentPostController extends BladeController {

	private final ITalentPostService talentPostService;

	@GetMapping("/mobile/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "公开达人教程详情", description = "只返回已发布且未删除的教程")
	public R<TalentPostVO> mobileDetail(@RequestParam Long id) {
		TalentPostEntity detail = talentPostService.getOne(Wrappers.<TalentPostEntity>lambdaQuery()
			.eq(TalentPostEntity::getId, id)
			.eq(TalentPostEntity::getStatus, 1)
			.eq(TalentPostEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (detail == null) return R.fail("教程不存在或已下架");
		return R.data(TalentPostWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "管理端教程详情")
	public R<TalentPostVO> detail(TalentPostEntity talentPost) {
		TalentPostEntity detail = talentPostService.getOne(Condition.getQueryWrapper(talentPost));
		return R.data(TalentPostWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "管理端教程分页")
	public R<IPage<TalentPostVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> talentPost, Query query) {
		IPage<TalentPostEntity> pages = talentPostService.page(
			Condition.getPage(query), Condition.getQueryWrapper(talentPost, TalentPostEntity.class));
		return R.data(TalentPostWrapper.build().pageVO(pages));
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "管理端教程关联分页")
	public R<IPage<TalentPostVO>> page(TalentPostVO talentPost, Query query) {
		return R.data(talentPostService.selectTalentPostPage(Condition.getPage(query), talentPost));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "管理端新增教程")
	public R save(@Valid @RequestBody TalentPostEntity talentPost) {
		return R.status(talentPostService.save(talentPost));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 14)
	@Operation(summary = "管理端修改教程")
	public R update(@Valid @RequestBody TalentPostEntity talentPost) {
		return R.status(talentPostService.updateById(talentPost));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 15)
	@Operation(summary = "管理端保存教程")
	public R submit(@Valid @RequestBody TalentPostEntity talentPost) {
		return R.status(talentPostService.saveOrUpdate(talentPost));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 16)
	@Operation(summary = "管理端删除教程")
	public R remove(@RequestParam String ids) {
		return R.status(talentPostService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-talentPost")
	@ApiOperationSupport(order = 17)
	@Operation(summary = "导出达人教程")
	public void exportTalentPost(@Parameter(hidden = true) @RequestParam Map<String, Object> talentPost,
		BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<TalentPostEntity> queryWrapper = Condition.getQueryWrapper(talentPost, TalentPostEntity.class);
		List<TalentPostExcel> list = talentPostService.exportTalentPost(queryWrapper);
		ExcelUtil.export(response, "达人教程" + DateUtil.time(), "达人教程", list, TalentPostExcel.class);
	}
}
