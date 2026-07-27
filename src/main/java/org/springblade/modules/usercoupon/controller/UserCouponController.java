package org.springblade.modules.usercoupon.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springblade.modules.usercoupon.excel.UserCouponExcel;
import org.springblade.modules.usercoupon.pojo.dto.UserCouponVerifyConfirmRequest;
import org.springblade.modules.usercoupon.pojo.entity.UserCouponEntity;
import org.springblade.modules.usercoupon.pojo.vo.UserCouponVO;
import org.springblade.modules.usercoupon.service.CouponVerificationService;
import org.springblade.modules.usercoupon.service.IUserCouponService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 用户优惠券控制器。
 *
 * <p>用户端只允许查看自己的券；管理端通用维护接口全部限制为管理员；
 * 二维码核销统一委托 {@link CouponVerificationService}，不再保留旧的直接核销旁路。</p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-usercoupon/userCoupon")
@Tag(name = "用户优惠券", description = "用户券查询、动态二维码与安全核销接口")
public class UserCouponController extends BladeController {

	private final IUserCouponService userCouponService;
	private final CouponVerificationService couponVerificationService;

	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "优惠券详情", description = "用户只能查看自己的优惠券，管理员可用于运营查询")
	public R<UserCouponVO> detail(@RequestParam Long id) {
		UserCouponEntity coupon = userCouponService.getById(id);
		if (coupon == null || Func.equals(coupon.getIsDeleted(), 1)) {
			return R.fail("优惠券不存在");
		}
		if (!AuthUtil.isAdministrator() && !AuthUtil.getUserId().equals(coupon.getUserId())) {
			return R.fail("无权查看该优惠券");
		}
		return R.data(userCouponService.buildCouponDetail(id));
	}

	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "我的优惠券", description = "按状态等条件查询当前用户自己的优惠券")
	public R<IPage<UserCouponVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> userCoupon, Query query) {
		userCoupon.put("userId", AuthUtil.getUserId());
		IPage<UserCouponEntity> entityPage = userCouponService.page(
			Condition.getPage(query),
			Condition.getQueryWrapper(userCoupon, UserCouponEntity.class)
		);
		IPage<UserCouponVO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
		result.setRecords(userCouponService.buildCouponList(entityPage.getRecords()));
		return R.data(result);
	}

	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "管理端优惠券分页", description = "运营人员按用户、模板和状态查询用户券")
	public R<IPage<UserCouponVO>> page(UserCouponVO userCoupon, Query query) {
		return R.data(userCouponService.selectUserCouponPage(Condition.getPage(query), userCoupon));
	}

	@IsAdmin
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "管理端发放优惠券", description = "运营人员人工发放用户券")
	public R save(@Valid @RequestBody UserCouponEntity userCoupon) {
		return R.status(userCouponService.save(userCoupon));
	}

	@IsAdmin
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "管理端修改优惠券", description = "仅用于运营纠错，不应代替领取和核销流程")
	public R update(@Valid @RequestBody UserCouponEntity userCoupon) {
		return R.status(userCouponService.updateById(userCoupon));
	}

	@IsAdmin
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "管理端保存优惠券", description = "运营人员新增或修改用户券")
	public R submit(@Valid @RequestBody UserCouponEntity userCoupon) {
		return R.status(userCouponService.saveOrUpdate(userCoupon));
	}

	@IsAdmin
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "管理端删除优惠券", description = "逻辑删除异常用户券")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(userCouponService.deleteLogic(Func.toLongList(ids)));
	}

	@IsAdmin
	@GetMapping("/export-userCoupon")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "导出用户优惠券", description = "按当前筛选条件导出用户券")
	public void exportUserCoupon(@Parameter(hidden = true) @RequestParam Map<String, Object> userCoupon,
								 BladeUser bladeUser,
								 HttpServletResponse response) {
		QueryWrapper<UserCouponEntity> queryWrapper = Condition.getQueryWrapper(userCoupon, UserCouponEntity.class);
		List<UserCouponExcel> list = userCouponService.exportUserCoupon(queryWrapper);
		ExcelUtil.export(response, "用户优惠券数据" + DateUtil.time(), "用户优惠券数据表", list, UserCouponExcel.class);
	}

	@IsAdmin
	@PostMapping("/release")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "释放锁券", description = "仅用于处理异常锁券，正常核销流程不得调用")
	public R<String> releaseCoupon(@RequestParam String couponNo) {
		String result = userCouponService.releaseCoupon(couponNo);
		return "释放成功".equals(result) ? R.data(result) : R.fail(result);
	}

	@GetMapping("/qrcode-token")
	@ApiOperationSupport(order = 10)
	@Operation(summary = "生成动态二维码", description = "为当前用户自己的可用优惠券生成短时动态令牌")
	public R<Map<String, Object>> qrCodeToken(@RequestParam Long userCouponId) {
		try {
			return R.data(couponVerificationService.createQrToken(userCouponId, AuthUtil.getUserId()));
		} catch (IllegalArgumentException | IllegalStateException exception) {
			return R.fail(exception.getMessage());
		}
	}

	@GetMapping("/verify-permission")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "获取核销权限", description = "返回当前账号可核销的真实业务范围")
	public R<Map<String, Object>> verifyPermission() {
		return R.data(couponVerificationService.getVerifierPermission(AuthUtil.getUserId()));
	}

	@PostMapping("/verify-scan")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "扫描优惠券二维码", description = "校验动态令牌、券状态和当前核销员授权范围")
	public R<UserCouponVO> verifyScan(@RequestBody Map<String, String> body) {
		try {
			return R.data(couponVerificationService.scan(body.get("qrToken"), AuthUtil.getUserId()));
		} catch (IllegalArgumentException exception) {
			return R.fail(exception.getMessage());
		}
	}

	@PostMapping("/verify-confirm")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "确认核销优惠券", description = "核销前必须完成扫码预检，支持整张或部分核销")
	public R<Map<String, Object>> verifyConfirm(@Valid @RequestBody UserCouponVerifyConfirmRequest request) {
		try {
			return R.data(couponVerificationService.confirm(request, AuthUtil.getUserId()));
		} catch (IllegalArgumentException exception) {
			return R.fail(exception.getMessage());
		}
	}

	@GetMapping("/verify-records")
	@ApiOperationSupport(order = 14)
	@Operation(summary = "优惠券核销记录", description = "用户只能查看自己的优惠券核销记录")
	public R<List<Map<String, Object>>> verifyRecords(@RequestParam Long userCouponId) {
		try {
			return R.data(couponVerificationService.getOwnerVerifyRecords(userCouponId, AuthUtil.getUserId()));
		} catch (IllegalArgumentException exception) {
			return R.fail(exception.getMessage());
		}
	}
}
