package org.springblade.modules.sportinvite.controller;

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
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.sportinvite.excel.SportInviteExcel;
import org.springblade.modules.sportinvite.pojo.entity.SportInviteEntity;
import org.springblade.modules.sportinvite.pojo.vo.SportInviteVO;
import org.springblade.modules.sportinvite.service.ISportInviteService;
import org.springblade.modules.sportinvite.wrapper.SportInviteWrapper;
import org.springblade.modules.sportinviteapply.pojo.entity.SportInviteApplyEntity;
import org.springblade.modules.sportinviteapply.pojo.vo.SportInviteApplyVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 绿动有约控制器。
 *
 * 管理端通用增删改查接口通过 {@link IsAdmin} 限制；小程序业务接口由服务层
 * 根据当前登录用户校验发布人、申请人和审核权限。
 *
 * @author BladeX
 * @since 2026-05-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-sportinvite/sportInvite")
@Tag(name = "绿动有约", description = "邀约发布、申请、审核及参与信息接口")
public class SportInviteController extends BladeController {

	private final ISportInviteService sportInviteService;

	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description = "兼容历史详情接口；小程序应使用 app-detail")
	public R<SportInviteVO> detail(SportInviteEntity sportInvite) {
		SportInviteEntity detail = sportInviteService.getOne(Condition.getQueryWrapper(sportInvite));
		return R.data(SportInviteWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "管理端分页", description = "运营人员查询邀约数据")
	public R<IPage<SportInviteVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> sportInvite, Query query) {
		IPage<SportInviteEntity> pages = sportInviteService.page(Condition.getPage(query), Condition.getQueryWrapper(sportInvite, SportInviteEntity.class));
		return R.data(SportInviteWrapper.build().pageVO(pages));
	}

	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "小程序邀约分页", description = "只返回可在小程序展示的邀约")
	public R<IPage<SportInviteVO>> page(SportInviteEntity sportInvite, Query query) {
		return R.data(sportInviteService.appPage(Condition.getPage(query), sportInvite));
	}

	@GetMapping("/app-detail")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "小程序邀约详情", description = "根据当前用户返回申请状态、操作权限和可见联系方式")
	public R<SportInviteVO> appDetail(@RequestParam Long id) {
		return R.data(sportInviteService.appDetail(id));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "管理端新增", description = "运营人员新增邀约")
	public R save(@Valid @RequestBody SportInviteEntity sportInvite) {
		return R.status(sportInviteService.save(sportInvite));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "管理端修改", description = "运营人员修改邀约")
	public R update(@Valid @RequestBody SportInviteEntity sportInvite) {
		return R.status(sportInviteService.updateById(sportInvite));
	}

	@PostMapping("/submit")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "发布邀约", description = "小程序用户发布邀约，发布人以后端登录身份为准")
	public R submit(@Valid @RequestBody SportInviteEntity sportInvite) {
		return R.status(sportInviteService.publish(sportInvite));
	}

	@PostMapping("/cancel")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "取消邀约", description = "兼容 Query 参数或 JSON Body 传入 id")
	public R cancel(@RequestParam(required = false) Long id,
					@RequestBody(required = false) Map<String, Object> body) {
		Long inviteId = resolveLong(id, body, "id");
		if (inviteId == null) {
			return R.fail("缺少邀约ID");
		}
		return R.status(sportInviteService.cancel(inviteId));
	}

	@PostMapping("/apply")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "申请加入", description = "申请人以后端登录身份为准")
	public R apply(@Valid @RequestBody SportInviteApplyEntity apply) {
		return R.status(sportInviteService.apply(apply));
	}

	@GetMapping("/myPublish")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "我发布的邀约", description = "当前用户发布的邀约分页")
	public R<IPage<SportInviteVO>> myPublish(Query query) {
		return R.data(sportInviteService.myPublish(Condition.getPage(query)));
	}

	@GetMapping("/myApply")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "我申请的邀约", description = "当前用户提交的申请分页")
	public R<IPage<SportInviteApplyVO>> myApply(Query query) {
		return R.data(sportInviteService.myApply(Condition.getPage(query)));
	}

	@GetMapping("/applyList")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "申请列表", description = "仅邀约发布人可查看对应申请列表")
	public R<IPage<SportInviteApplyVO>> applyList(Query query, @RequestParam Long inviteId, String applyStatus) {
		return R.data(sportInviteService.applyList(Condition.getPage(query), inviteId, applyStatus));
	}

	@PostMapping("/audit")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "审核申请", description = "兼容 Query 参数或 JSON Body；仅邀约发布人可操作")
	public R audit(@RequestParam(required = false) Long applyId,
				   @RequestParam(required = false) String auditAction,
				   @RequestParam(required = false) String auditRemark,
				   @RequestBody(required = false) Map<String, Object> body) {
		Map<String, Object> safeBody = body == null ? Collections.emptyMap() : body;
		Long targetApplyId = resolveLong(applyId, safeBody, "applyId");
		String targetAction = resolveString(auditAction, safeBody, "auditAction");
		String targetRemark = resolveString(auditRemark, safeBody, "auditRemark");
		if (targetApplyId == null) {
			return R.fail("缺少申请ID");
		}
		if (targetAction == null || targetAction.trim().isEmpty()) {
			return R.fail("缺少审核动作");
		}
		return R.status(sportInviteService.audit(targetApplyId, targetAction, targetRemark));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 14)
	@Operation(summary = "管理端删除", description = "运营人员逻辑删除邀约")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(sportInviteService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-sportInvite")
	@ApiOperationSupport(order = 15)
	@Operation(summary = "导出邀约", description = "运营人员按筛选条件导出邀约")
	public void exportSportInvite(@Parameter(hidden = true) @RequestParam Map<String, Object> sportInvite,
								  BladeUser bladeUser,
								  HttpServletResponse response) {
		QueryWrapper<SportInviteEntity> queryWrapper = Condition.getQueryWrapper(sportInvite, SportInviteEntity.class);
		List<SportInviteExcel> list = sportInviteService.exportSportInvite(queryWrapper);
		ExcelUtil.export(response, "绿动有约数据" + DateUtil.time(), "绿动有约数据表", list, SportInviteExcel.class);
	}

	private Long resolveLong(Long queryValue, Map<String, Object> body, String key) {
		if (queryValue != null) {
			return queryValue;
		}
		if (body == null || body.get(key) == null) {
			return null;
		}
		try {
			return Long.valueOf(String.valueOf(body.get(key)));
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private String resolveString(String queryValue, Map<String, Object> body, String key) {
		if (queryValue != null) {
			return queryValue;
		}
		if (body == null || body.get(key) == null) {
			return null;
		}
		return String.valueOf(body.get(key));
	}
}
