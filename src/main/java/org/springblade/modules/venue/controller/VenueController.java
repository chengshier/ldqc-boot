package org.springblade.modules.venue.controller;

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
import org.springblade.modules.venue.excel.VenueExcel;
import org.springblade.modules.venue.pojo.entity.VenueEntity;
import org.springblade.modules.venue.pojo.vo.VenueVO;
import org.springblade.modules.venue.service.IVenueService;
import org.springblade.modules.venue.wrapper.VenueWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 体育场馆公开查询和管理端维护接口。 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-venue/venue")
@Tag(name = "体育场馆", description = "场馆公开列表、详情和管理端维护接口")
public class VenueController extends BladeController {

	private final IVenueService venueService;

	@GetMapping("/mobile/page")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "公开场馆分页", description = "支持关键词、类型和经纬度范围筛选")
	public R<IPage<VenueVO>> mobilePage(Query query,
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) Long typeId,
		@RequestParam(required = false) BigDecimal minLongitude,
		@RequestParam(required = false) BigDecimal maxLongitude,
		@RequestParam(required = false) BigDecimal minLatitude,
		@RequestParam(required = false) BigDecimal maxLatitude) {
		QueryWrapper<VenueEntity> wrapper = new QueryWrapper<VenueEntity>()
			.eq("status", 1)
			.eq("is_deleted", 0)
			.eq(typeId != null, "type_id", typeId)
			.ge(minLongitude != null, "longitude", minLongitude)
			.le(maxLongitude != null, "longitude", maxLongitude)
			.ge(minLatitude != null, "latitude", minLatitude)
			.le(maxLatitude != null, "latitude", maxLatitude);
		if (!Func.isBlank(keyword)) {
			wrapper.and(item -> item.like("name", keyword).or().like("address", keyword).or().like("tags", keyword));
		}
		wrapper.orderByDesc("sort_order").orderByDesc("rating").orderByDesc("update_time");
		IPage<VenueEntity> page = venueService.page(Condition.getPage(query), wrapper);
		return R.data(VenueWrapper.build().pageVO(page));
	}

	@GetMapping("/mobile/detail")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "公开场馆详情")
	public R<VenueVO> mobileDetail(@RequestParam Long id) {
		VenueEntity detail = venueService.getOne(Wrappers.<VenueEntity>lambdaQuery()
			.eq(VenueEntity::getId, id)
			.eq(VenueEntity::getStatus, 1)
			.eq(VenueEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (detail == null) return R.fail("场馆不存在或已停用");
		return R.data(VenueWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "管理端场馆详情")
	public R<VenueVO> detail(VenueEntity venue) {
		VenueEntity detail = venueService.getOne(Condition.getQueryWrapper(venue));
		return R.data(VenueWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "管理端场馆分页")
	public R<IPage<VenueVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> venue, Query query) {
		IPage<VenueEntity> pages = venueService.page(
			Condition.getPage(query), Condition.getQueryWrapper(venue, VenueEntity.class));
		return R.data(VenueWrapper.build().pageVO(pages));
	}

	@IsAdmin
	@GetMapping("/listDic")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "管理端场馆选择器")
	public R<List<VenueEntity>> listDic() {
		return R.data(venueService.list(Wrappers.<VenueEntity>lambdaQuery()
			.eq(VenueEntity::getIsDeleted, 0).orderByAsc(VenueEntity::getName)));
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "管理端场馆关联分页")
	public R<IPage<VenueVO>> page(VenueVO venue, Query query) {
		return R.data(venueService.selectVenuePage(Condition.getPage(query), venue));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 14)
	@Operation(summary = "管理端新增场馆")
	public R save(@Valid @RequestBody VenueEntity venue) {
		return R.status(venueService.save(venue));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 15)
	@Operation(summary = "管理端修改场馆")
	public R update(@Valid @RequestBody VenueEntity venue) {
		return R.status(venueService.updateById(venue));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 16)
	@Operation(summary = "管理端保存场馆")
	public R submit(@Valid @RequestBody VenueEntity venue) {
		return R.status(venueService.saveOrUpdate(venue));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 17)
	@Operation(summary = "管理端删除场馆")
	public R remove(@RequestParam String ids) {
		return R.status(venueService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-venue")
	@ApiOperationSupport(order = 18)
	@Operation(summary = "导出场馆数据")
	public void exportVenue(@Parameter(hidden = true) @RequestParam Map<String, Object> venue,
		BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<VenueEntity> queryWrapper = Condition.getQueryWrapper(venue, VenueEntity.class);
		List<VenueExcel> list = venueService.exportVenue(queryWrapper);
		ExcelUtil.export(response, "体育场馆" + DateUtil.time(), "体育场馆", list, VenueExcel.class);
	}
}
