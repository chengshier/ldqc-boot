package org.springblade.modules.competitionsignup.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.competitionsignup.excel.CompetitionSignupExcel;
import org.springblade.modules.competitionsignup.pojo.entity.CompetitionSignupEntity;
import org.springblade.modules.competitionsignup.pojo.vo.CompetitionSignupVO;
import org.springblade.modules.competitionsignup.service.CompetitionSignupWorkflowService;
import org.springblade.modules.competitionsignup.service.ICompetitionSignupService;
import org.springblade.modules.competitionsignup.wrapper.CompetitionSignupWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 赛事报名订单控制器。
 *
 * <p>管理端通用查询与维护全部要求管理员身份；小程序只能通过 mobile 业务接口
 * 创建和查看自己的报名订单。</p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-competitionsignup/competitionSignup")
@Tag(name = "赛事报名订单", description = "赛事报名、订单状态和运营查询接口")
public class CompetitionSignupController extends BladeController {

	private final ICompetitionSignupService competitionSignupService;
	private final CompetitionSignupWorkflowService workflowService;

	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "管理端报名详情")
	public R<CompetitionSignupVO> detail(CompetitionSignupEntity competitionSignup) {
		CompetitionSignupEntity detail = competitionSignupService.getOne(Condition.getQueryWrapper(competitionSignup));
		return R.data(CompetitionSignupWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "管理端报名分页")
	public R<IPage<CompetitionSignupVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> competitionSignup, Query query) {
		IPage<CompetitionSignupEntity> pages = competitionSignupService.page(
			Condition.getPage(query), Condition.getQueryWrapper(competitionSignup, CompetitionSignupEntity.class));
		return R.data(CompetitionSignupWrapper.build().pageVO(pages));
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "管理端报名关联分页")
	public R<IPage<CompetitionSignupVO>> page(CompetitionSignupVO competitionSignup, Query query) {
		return R.data(competitionSignupService.selectCompetitionSignupPage(Condition.getPage(query), competitionSignup));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "管理端新增报名记录", description = "仅用于数据修复，不得替代用户报名流程")
	public R save(@Valid @RequestBody CompetitionSignupEntity competitionSignup) {
		return R.status(competitionSignupService.save(competitionSignup));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "管理端修改报名记录")
	public R update(@Valid @RequestBody CompetitionSignupEntity competitionSignup) {
		return R.status(competitionSignupService.updateById(competitionSignup));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "管理端保存报名记录")
	public R submit(@Valid @RequestBody CompetitionSignupEntity competitionSignup) {
		return R.status(competitionSignupService.saveOrUpdate(competitionSignup));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "管理端逻辑删除报名记录")
	public R remove(@RequestParam String ids) {
		return R.status(competitionSignupService.deleteLogic(Func.toLongList(ids)));
	}

	@PostMapping("/mobile/create")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "提交赛事报名", description = "价格、支付状态、报名窗口和名额均由后端校验")
	public R<Map<String, Object>> mobileCreate(@RequestBody Map<String, Object> body) {
		return R.data(workflowService.create(body, AuthUtil.getUserId()));
	}

	@GetMapping("/mobile/page")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "我的赛事报名订单")
	public R<IPage<Map<String, Object>>> mobilePage(@RequestParam(defaultValue = "1") Integer current,
		@RequestParam(defaultValue = "20") Integer size) {
		return R.data(workflowService.myPage(current, size, AuthUtil.getUserId()));
	}

	@GetMapping("/mobile/detail")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "我的赛事报名详情")
	public R<Map<String, Object>> mobileDetail(@RequestParam Long id) {
		return R.data(workflowService.myDetail(id, AuthUtil.getUserId()));
	}

	@PostMapping("/mobile/cancel")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "取消赛事报名", description = "免费或未支付订单可取消并释放名额；已支付订单必须走退款")
	public R mobileCancel(@RequestBody Map<String, Object> body) {
		Long id = Func.toLong(body.get("id"));
		if (id == null) return R.fail("缺少报名订单ID");
		workflowService.cancel(id, Func.toStr(body.get("reason"), "用户主动取消"), AuthUtil.getUserId());
		return R.success("报名已取消");
	}

	@IsAdmin
	@GetMapping("/export-competitionSignup")
	@ApiOperationSupport(order = 20)
	@Operation(summary = "导出赛事报名数据")
	public void exportCompetitionSignup(@Parameter(hidden = true) @RequestParam Map<String, Object> competitionSignup,
		BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<CompetitionSignupEntity> queryWrapper = Condition.getQueryWrapper(competitionSignup, CompetitionSignupEntity.class);
		List<CompetitionSignupExcel> list = competitionSignupService.exportCompetitionSignup(queryWrapper);
		ExcelUtil.export(response, "赛事报名数据" + DateUtil.time(), "赛事报名", list, CompetitionSignupExcel.class);
	}
}
