package org.springblade.modules.couponverifierscope.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.couponverifierscope.pojo.entity.CouponVerifierScopeEntity;
import org.springblade.modules.couponverifierscope.service.ICouponVerifierScopeService;
import org.springblade.modules.mallproduct.pojo.entity.MallProductEntity;
import org.springblade.modules.mallproduct.service.IMallProductService;
import org.springblade.modules.outdoor.pojo.entity.OutdoorEntity;
import org.springblade.modules.outdoor.service.IOutdoorService;
import org.springblade.modules.training.pojo.entity.TrainingEntity;
import org.springblade.modules.training.service.ITrainingService;
import org.springblade.modules.venue.pojo.entity.VenueEntity;
import org.springblade.modules.venue.service.IVenueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@IsAdmin
@RestController
@AllArgsConstructor
@RequestMapping("blade-couponverifierscope/couponVerifierScope")
@Tag(name = "优惠券核销员授权", description = "核销账号及适用范围管理")
public class CouponVerifierScopeController extends BladeController {
	private final ICouponVerifierScopeService couponVerifierScopeService;
	private final IVenueService venueService;
	private final IOutdoorService outdoorService;
	private final ITrainingService trainingService;
	private final IMallProductService mallProductService;

	@GetMapping("/detail")
	@Operation(summary = "授权详情")
	public R<CouponVerifierScopeEntity> detail(@RequestParam Long id) { return R.data(couponVerifierScopeService.getById(id)); }

	@GetMapping("/list")
	@Operation(summary = "授权分页")
	public R<IPage<CouponVerifierScopeEntity>> list(@RequestParam Map<String, Object> params, Query query) {
		return R.data(couponVerifierScopeService.page(Condition.getPage(query), Condition.getQueryWrapper(params, CouponVerifierScopeEntity.class)));
	}

	@PostMapping("/submit")
	@Operation(summary = "新增或修改授权")
	public R<Boolean> submit(@RequestBody CouponVerifierScopeEntity scope) {
		if (scope.getVerifierUserId() == null || Func.isBlank(scope.getScopeType()) || Func.isBlank(scope.getScopeRefId())) return R.fail("核销员、授权类型和范围ID不能为空");
		String scopeName = resolveScopeName(scope.getScopeType(), scope.getScopeRefId());
		if (Func.isBlank(scopeName)) return R.fail("授权类型与授权范围不匹配或范围不存在");
		scope.setVenueName(scopeName);
		return R.status(couponVerifierScopeService.saveOrUpdate(scope));
	}

	@PostMapping("/remove")
	@Operation(summary = "删除授权")
	public R<Boolean> remove(@RequestParam String ids) { return R.status(couponVerifierScopeService.deleteLogic(Func.toLongList(ids))); }

	private String resolveScopeName(String scopeType, String scopeRefId) {
		if ("ALL".equals(scopeType)) return "ALL".equals(scopeRefId) ? "全场通用" : null;
		Long scopeId;
		try {
			scopeId = Long.valueOf(scopeRefId);
		} catch (NumberFormatException ignored) {
			return null;
		}
		if ("VENUE".equals(scopeType)) {
			VenueEntity venue = venueService.getById(scopeId);
			return venue == null ? null : venue.getName();
		}
		if ("CAMP".equals(scopeType)) {
			OutdoorEntity outdoor = outdoorService.getById(scopeId);
			return outdoor == null ? null : outdoor.getTitle();
		}
		if ("COURSE".equals(scopeType)) {
			TrainingEntity training = trainingService.getById(scopeId);
			return training == null ? null : training.getTitle();
		}
		if ("GOODS".equals(scopeType)) {
			MallProductEntity product = mallProductService.getById(scopeId);
			return product == null ? null : product.getProductName();
		}
		return null;
	}
}
