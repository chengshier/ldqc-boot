package org.springblade.modules.follow.controller;

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
import org.springblade.modules.follow.excel.FollowExcel;
import org.springblade.modules.follow.pojo.dto.FollowDTO;
import org.springblade.modules.follow.pojo.entity.FollowEntity;
import org.springblade.modules.follow.pojo.vo.FollowVO;
import org.springblade.modules.follow.service.FollowBusinessService;
import org.springblade.modules.follow.service.IFollowService;
import org.springblade.modules.follow.wrapper.FollowWrapper;
import org.springblade.modules.system.pojo.vo.TrendVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 关注关系控制器。
 *
 * <p>用户业务接口的关注发起人全部取当前登录用户，客户端提交的 uid 不再作为身份依据。
 * 通用 CRUD 仅供管理端排查异常关系。</p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-follow/follow")
@Tag(name = "关注关系", description = "关注、粉丝、关注流与管理端查询接口")
public class FollowController extends BladeController {

	private final IFollowService followService;
	private final FollowBusinessService businessService;

	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "管理端关注关系详情")
	public R<FollowVO> detail(FollowEntity follow) {
		FollowEntity detail = followService.getOne(Condition.getQueryWrapper(follow));
		return R.data(FollowWrapper.build().entityVO(detail));
	}

	@IsAdmin
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "管理端关注关系分页")
	public R<IPage<FollowVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> follow, Query query) {
		IPage<FollowEntity> pages = followService.page(
			Condition.getPage(query), Condition.getQueryWrapper(follow, FollowEntity.class));
		return R.data(FollowWrapper.build().pageVO(pages));
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "管理端关注关联分页")
	public R<IPage<FollowVO>> page(FollowVO follow, Query query) {
		return R.data(followService.selectFollowPage(Condition.getPage(query), follow));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "管理端新增关注关系", description = "仅用于数据修复，正常关注必须走 mobile/follow")
	public R save(@Valid @RequestBody FollowEntity follow) {
		return R.status(followService.save(follow));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "管理端修改关注关系")
	public R update(@Valid @RequestBody FollowEntity follow) {
		return R.status(followService.updateById(follow));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "管理端保存关注关系")
	public R submit(@Valid @RequestBody FollowEntity follow) {
		return R.status(followService.saveOrUpdate(follow));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "管理端删除关注关系")
	public R remove(@RequestParam String ids) {
		return R.status(followService.deleteLogic(Func.toLongList(ids)));
	}

	@GetMapping("/mobile/feed")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "我的关注动态", description = "只返回当前登录用户已关注账号的审核通过内容")
	public R<List<TrendVO>> mobileFeed(@RequestParam(defaultValue = "1") long page,
		@RequestParam(defaultValue = "20") long limit) {
		return R.data(businessService.feed(page, limit, AuthUtil.getUserId()));
	}

	@GetMapping("/mobile/connections")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "关注或粉丝列表", description = "type：FOLLOWING 或 FOLLOWERS；未传 targetUserId 时查看自己")
	public R<IPage<Map<String, Object>>> mobileConnections(@RequestParam(defaultValue = "1") long page,
		@RequestParam(defaultValue = "20") long limit,
		@RequestParam(required = false) Long targetUserId,
		@RequestParam(defaultValue = "FOLLOWING") String type) {
		return R.data(businessService.connections(page, limit, targetUserId, type, AuthUtil.getUserId()));
	}

	@GetMapping("/mobile/status")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "查询关注状态")
	public R<Map<String, Object>> mobileStatus(@RequestParam Long targetUserId) {
		return R.data(businessService.status(AuthUtil.getUserId(), targetUserId));
	}

	@GetMapping("/mobile/counts")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "查询关注与粉丝数量")
	public R<Map<String, Object>> mobileCounts(@RequestParam Long targetUserId) {
		return R.data(businessService.counts(targetUserId));
	}

	@PostMapping("/mobile/follow")
	@ApiOperationSupport(order = 14)
	@Operation(summary = "关注用户", description = "关注发起人以后端登录态为准")
	public R<Map<String, Object>> mobileFollow(@RequestBody Map<String, Object> body) {
		Long targetUserId = resolveTargetUserId(body);
		return R.data(businessService.follow(AuthUtil.getUserId(), targetUserId));
	}

	@PostMapping("/mobile/unfollow")
	@ApiOperationSupport(order = 15)
	@Operation(summary = "取消关注")
	public R<Map<String, Object>> mobileUnfollow(@RequestBody Map<String, Object> body) {
		Long targetUserId = resolveTargetUserId(body);
		return R.data(businessService.unfollow(AuthUtil.getUserId(), targetUserId));
	}

	/* 以下兼容旧小程序路径，但忽略客户端 uid，只使用当前登录用户。 */
	@RequestMapping("/getAllFollowTrends")
	@Operation(summary = "兼容：我的关注动态")
	public R<List<TrendVO>> getAllFollowTrends(@RequestParam long page, @RequestParam long limit,
		@RequestParam(required = false) String uid) {
		return R.data(businessService.feed(page, limit, AuthUtil.getUserId()));
	}

	@RequestMapping("/getAllFriend")
	@Operation(summary = "兼容：粉丝或关注列表")
	public R<IPage<Map<String, Object>>> getAllFriend(@RequestParam long page, @RequestParam long limit,
		@RequestParam(required = false) String uid, @RequestParam Integer type) {
		Long targetUserId = Func.isBlank(uid) ? AuthUtil.getUserId() : Func.toLong(uid);
		return R.data(businessService.connections(page, limit, targetUserId,
			Func.equals(type, 0) ? "FOLLOWERS" : "FOLLOWING", AuthUtil.getUserId()));
	}

	@PostMapping("/followUser")
	@Operation(summary = "兼容：关注用户")
	public R<Map<String, Object>> followUser(@RequestBody FollowDTO followDTO) {
		return R.data(businessService.follow(AuthUtil.getUserId(), followDTO.getFid()));
	}

	@RequestMapping("/isFollow")
	@Operation(summary = "兼容：查询关注状态")
	public R<Boolean> isFollow(@RequestParam(required = false) String uid, @RequestParam String fid) {
		Map<String, Object> status = businessService.status(AuthUtil.getUserId(), Func.toLong(fid));
		return R.data(Boolean.TRUE.equals(status.get("following")));
	}

	@PostMapping("/clearFollow")
	@Operation(summary = "兼容：取消关注")
	public R<Map<String, Object>> clearFollow(@RequestBody FollowDTO followDTO) {
		return R.data(businessService.unfollow(AuthUtil.getUserId(), followDTO.getFid()));
	}

	@IsAdmin
	@GetMapping("/export-follow")
	@ApiOperationSupport(order = 30)
	@Operation(summary = "导出关注关系")
	public void exportFollow(@Parameter(hidden = true) @RequestParam Map<String, Object> follow,
		BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<FollowEntity> queryWrapper = Condition.getQueryWrapper(follow, FollowEntity.class);
		List<FollowExcel> list = followService.exportFollow(queryWrapper);
		ExcelUtil.export(response, "关注关系" + DateUtil.time(), "关注关系", list, FollowExcel.class);
	}

	private Long resolveTargetUserId(Map<String, Object> body) {
		Long targetUserId = Func.toLong(body.get("targetUserId"));
		if (targetUserId == null) targetUserId = Func.toLong(body.get("fid"));
		if (targetUserId == null) throw new org.springblade.core.log.exception.ServiceException("缺少目标用户ID");
		return targetUserId;
	}
}
