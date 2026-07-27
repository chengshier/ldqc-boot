package org.springblade.modules.bannerposition.controller;

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
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.modules.bannerposition.pojo.entity.BannerPositionEntity;
import org.springblade.modules.bannerposition.pojo.vo.BannerPositionVO;
import org.springblade.modules.bannerposition.excel.BannerPositionExcel;
import org.springblade.modules.bannerposition.wrapper.BannerPositionWrapper;
import org.springblade.modules.bannerposition.service.IBannerPositionService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@AllArgsConstructor
@RequestMapping("blade-bannerposition/bannerPosition")
@Tag(name = "宣传Banner位置表", description = "宣传Banner位置表接口")
public class BannerPositionController extends BladeController {

	private final IBannerPositionService bannerPositionService;

	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description = "传入bannerPosition")
	public R<BannerPositionVO> detail(BannerPositionEntity bannerPosition) {
		BannerPositionEntity detail = bannerPositionService.getOne(Condition.getQueryWrapper(bannerPosition));
		return R.data(BannerPositionWrapper.build().entityVO(detail));
	}

	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description = "传入bannerPosition")
	public R<IPage<BannerPositionVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> bannerPosition, Query query) {
		IPage<BannerPositionEntity> pages = bannerPositionService.page(Condition.getPage(query), Condition.getQueryWrapper(bannerPosition, BannerPositionEntity.class));
		return R.data(BannerPositionWrapper.build().pageVO(pages));
	}

	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description = "传入bannerPosition")
	public R<IPage<BannerPositionVO>> page(BannerPositionVO bannerPosition, Query query) {
		IPage<BannerPositionVO> pages = bannerPositionService.selectBannerPositionPage(Condition.getPage(query), bannerPosition);
		return R.data(pages);
	}

	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description = "传入bannerPosition")
	public R save(@Valid @RequestBody BannerPositionEntity bannerPosition) {
		return R.status(bannerPositionService.save(bannerPosition));
	}

	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description = "传入bannerPosition")
	public R update(@Valid @RequestBody BannerPositionEntity bannerPosition) {
		return R.status(bannerPositionService.updateById(bannerPosition));
	}

	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description = "传入bannerPosition")
	public R submit(@Valid @RequestBody BannerPositionEntity bannerPosition) {
		return R.status(bannerPositionService.saveOrUpdate(bannerPosition));
	}

	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(bannerPositionService.deleteLogic(Func.toLongList(ids)));
	}

	@GetMapping("/select")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "下拉数据", description = "按状态筛选Banner位置")
	public R<List<BannerPositionEntity>> select(@RequestParam(required = false) Integer status) {
		QueryWrapper<BannerPositionEntity> queryWrapper = Wrappers.query();
		queryWrapper.lambda()
			.eq(BannerPositionEntity::getIsDeleted, 0)
			.eq(status != null, BannerPositionEntity::getStatus, status)
			.orderByAsc(BannerPositionEntity::getSort)
			.orderByAsc(BannerPositionEntity::getCreateTime);
		return R.data(bannerPositionService.list(queryWrapper));
	}

	@IsAdmin
	@GetMapping("/export-bannerPosition")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description = "传入bannerPosition")
	public void exportBannerPosition(@Parameter(hidden = true) @RequestParam Map<String, Object> bannerPosition, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<BannerPositionEntity> queryWrapper = Condition.getQueryWrapper(bannerPosition, BannerPositionEntity.class);
		List<BannerPositionExcel> list = bannerPositionService.exportBannerPosition(queryWrapper);
		ExcelUtil.export(response, "宣传Banner位置表数据" + DateUtil.time(), "宣传Banner位置表数据表", list, BannerPositionExcel.class);
	}
}
