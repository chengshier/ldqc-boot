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

import java.util.List;
import java.util.Map;

/**
 * 运动邀约表 控制器
 *
 * @author BladeX
 * @since 2026-05-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-sportinvite/sportInvite")
@Tag(name = "运动邀约表", description = "运动邀约表接口")
public class SportInviteController extends BladeController {

	private final ISportInviteService sportInviteService;

	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description  = "传入sportInvite")
	public R<SportInviteVO> detail(SportInviteEntity sportInvite) {
		SportInviteEntity detail = sportInviteService.getOne(Condition.getQueryWrapper(sportInvite));
		return R.data(SportInviteWrapper.build().entityVO(detail));
	}

	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description  = "传入sportInvite")
	public R<IPage<SportInviteVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> sportInvite, Query query) {
		IPage<SportInviteEntity> pages = sportInviteService.page(Condition.getPage(query), Condition.getQueryWrapper(sportInvite, SportInviteEntity.class));
		return R.data(SportInviteWrapper.build().pageVO(pages));
	}

	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "小程序邀约分页", description  = "传入sportInvite")
	public R<IPage<SportInviteVO>> page(SportInviteEntity sportInvite, Query query) {
		return R.data(sportInviteService.appPage(Condition.getPage(query), sportInvite));
	}

	@GetMapping("/app-detail")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "小程序邀约详情", description = "传入id")
	public R<SportInviteVO> appDetail(@RequestParam Long id) {
		return R.data(sportInviteService.appDetail(id));
	}

	@PostMapping("/save")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "新增", description  = "传入sportInvite")
	public R save(@Valid @RequestBody SportInviteEntity sportInvite) {
		return R.status(sportInviteService.save(sportInvite));
	}

	@PostMapping("/update")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "修改", description  = "传入sportInvite")
	public R update(@Valid @RequestBody SportInviteEntity sportInvite) {
		return R.status(sportInviteService.updateById(sportInvite));
	}

	@PostMapping("/submit")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "发布邀约", description  = "传入sportInvite")
	public R submit(@Valid @RequestBody SportInviteEntity sportInvite) {
		return R.status(sportInviteService.publish(sportInvite));
	}

	@PostMapping("/cancel")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "取消邀约", description = "传入id")
	public R cancel(@RequestParam Long id) {
		return R.status(sportInviteService.cancel(id));
	}

	@PostMapping("/apply")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "申请加入", description = "传入sportInviteApply")
	public R apply(@Valid @RequestBody SportInviteApplyEntity apply) {
		return R.status(sportInviteService.apply(apply));
	}

	@GetMapping("/myPublish")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "我发布的邀约", description = "分页")
	public R<IPage<SportInviteVO>> myPublish(Query query) {
		return R.data(sportInviteService.myPublish(Condition.getPage(query)));
	}

	@GetMapping("/myApply")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "我申请的邀约", description = "分页")
	public R<IPage<SportInviteApplyVO>> myApply(Query query) {
		return R.data(sportInviteService.myApply(Condition.getPage(query)));
	}

	@GetMapping("/applyList")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "申请列表", description = "传入inviteId/applyStatus")
	public R<IPage<SportInviteApplyVO>> applyList(Query query, @RequestParam Long inviteId, String applyStatus) {
		return R.data(sportInviteService.applyList(Condition.getPage(query), inviteId, applyStatus));
	}

	@PostMapping("/audit")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "审核申请", description = "传入applyId/auditAction/auditRemark")
	public R audit(@RequestParam Long applyId, @RequestParam String auditAction, String auditRemark) {
		return R.status(sportInviteService.audit(applyId, auditAction, auditRemark));
	}

	@PostMapping("/remove")
	@ApiOperationSupport(order = 14)
	@Operation(summary = "逻辑删除", description  = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(sportInviteService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-sportInvite")
	@ApiOperationSupport(order = 15)
	@Operation(summary = "导出数据", description  = "传入sportInvite")
	public void exportSportInvite(@Parameter(hidden = true) @RequestParam Map<String, Object> sportInvite, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<SportInviteEntity> queryWrapper = Condition.getQueryWrapper(sportInvite, SportInviteEntity.class);
		List<SportInviteExcel> list = sportInviteService.exportSportInvite(queryWrapper);
		ExcelUtil.export(response, "运动邀约表数据" + DateUtil.time(), "运动邀约表数据表", list, SportInviteExcel.class);
	}

}
