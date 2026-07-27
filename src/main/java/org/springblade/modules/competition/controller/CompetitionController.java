package org.springblade.modules.competition.controller;

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
import org.springblade.modules.competition.excel.CompetitionExcel;
import org.springblade.modules.competition.pojo.entity.CompetitionEntity;
import org.springblade.modules.competition.pojo.vo.CompetitionVO;
import org.springblade.modules.competition.service.ICompetitionService;
import org.springblade.modules.competition.wrapper.CompetitionWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 赛事公开展示和管理端维护接口。 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-competition/competition")
@Tag(name = "赛事", description = "赛事公开展示与管理接口")
public class CompetitionController extends BladeController {

	private final ICompetitionService competitionService;

	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "管理端赛事详情")
	public R<CompetitionVO> detail(CompetitionEntity competition) {
		CompetitionEntity detail = competitionService.getOne(Condition.getQueryWrapper(competition));
		return R.data(CompetitionWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "管理端赛事分页")
	public R<IPage<CompetitionVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> competition, Query query) {
		IPage<CompetitionEntity> pages = competitionService.page(
			Condition.getPage(query), Condition.getQueryWrapper(competition, CompetitionEntity.class));
		return R.data(CompetitionWrapper.build().pageVO(pages));
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "管理端赛事关联分页")
	public R<IPage<CompetitionVO>> page(CompetitionVO competition, Query query) {
		return R.data(competitionService.selectCompetitionPage(Condition.getPage(query), competition));
	}

	@GetMapping("/mobile/page")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "公开赛事列表", description = "仅返回报名中、进行中或已结束的赛事")
	public R<IPage<CompetitionVO>> mobilePage(Query query, @RequestParam(required = false) Integer status) {
		IPage<CompetitionEntity> page = competitionService.page(Condition.getPage(query),
			Wrappers.<CompetitionEntity>lambdaQuery()
				.eq(status != null && status >= 1 && status <= 3, CompetitionEntity::getStatus, status)
				.in(status == null, CompetitionEntity::getStatus, 1, 2, 3)
				.eq(CompetitionEntity::getIsDeleted, 0)
				.orderByAsc(CompetitionEntity::getStatus)
				.orderByAsc(CompetitionEntity::getStartTime));
		return R.data(CompetitionWrapper.build().pageVO(page));
	}

	@GetMapping("/mobile/detail")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "公开赛事详情")
	public R<CompetitionVO> mobileDetail(@RequestParam Long id) {
		CompetitionEntity detail = competitionService.getOne(Wrappers.<CompetitionEntity>lambdaQuery()
			.eq(CompetitionEntity::getId, id)
			.in(CompetitionEntity::getStatus, 1, 2, 3)
			.eq(CompetitionEntity::getIsDeleted, 0)
			.last("limit 1"));
		if (detail == null) return R.fail("赛事不存在或已下架");
		return R.data(CompetitionWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 20)
	@Operation(summary = "新增赛事")
	public R save(@Valid @RequestBody CompetitionEntity competition) {
		normalize(competition);
		return R.status(competitionService.save(competition));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 21)
	@Operation(summary = "修改赛事")
	public R update(@Valid @RequestBody CompetitionEntity competition) {
		normalize(competition);
		return R.status(competitionService.updateById(competition));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 22)
	@Operation(summary = "保存赛事")
	public R submit(@Valid @RequestBody CompetitionEntity competition) {
		normalize(competition);
		return R.status(competitionService.saveOrUpdate(competition));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 23)
	@Operation(summary = "逻辑删除赛事")
	public R remove(@RequestParam String ids) {
		return R.status(competitionService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-competition")
	@ApiOperationSupport(order = 24)
	@Operation(summary = "导出赛事数据")
	public void exportCompetition(@Parameter(hidden = true) @RequestParam Map<String, Object> competition,
		BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<CompetitionEntity> queryWrapper = Condition.getQueryWrapper(competition, CompetitionEntity.class);
		List<CompetitionExcel> list = competitionService.exportCompetition(queryWrapper);
		ExcelUtil.export(response, "赛事数据" + DateUtil.time(), "赛事", list, CompetitionExcel.class);
	}

	private void normalize(CompetitionEntity competition) {
		if (competition.getParticipantCount() == null || competition.getParticipantCount() < 0) competition.setParticipantCount(0);
		if (competition.getMaxPeoplePerOrder() == null || competition.getMaxPeoplePerOrder() <= 0) competition.setMaxPeoplePerOrder(1);
		if (competition.getPrice() == null || competition.getPrice().signum() <= 0) {
			competition.setPrice(java.math.BigDecimal.ZERO);
			competition.setPaymentMode("FREE");
		} else if (Func.isBlank(competition.getPaymentMode())) {
			competition.setPaymentMode("WECHAT");
		}
		if (competition.getSignupEndTime() == null) competition.setSignupEndTime(competition.getStartTime());
	}
}
