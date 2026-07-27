package org.springblade.modules.mallproduct.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.mallproduct.pojo.entity.MallProductEntity;
import org.springblade.modules.mallproduct.service.MallProductOperationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 积分商城商品运营工作台接口。
 */
@IsAdmin
@RestController
@RequiredArgsConstructor
@RequestMapping("blade-mall/product-admin")
@Tag(name = "积分商城商品运营", description = "运营人员维护商品资料、库存、兑换规则和上下架")
public class MallProductOperationController {

	private final MallProductOperationService operationService;

	@GetMapping("/page")
	@Operation(summary = "运营商品分页")
	public R<IPage<MallProductEntity>> page(Query query, @RequestParam Map<String, Object> filters) {
		return R.data(operationService.page(query, filters));
	}

	@GetMapping("/detail")
	@Operation(summary = "运营商品详情")
	public R<MallProductEntity> detail(@RequestParam Long id) {
		return R.data(operationService.detail(id));
	}

	@PostMapping("/save")
	@Operation(summary = "保存商品", description = "优惠券不能在商城商品中维护")
	public R<MallProductEntity> save(@RequestBody Map<String, Object> body) {
		return R.data(operationService.save(body));
	}

	@PostMapping("/status")
	@Operation(summary = "商品上架或下架", description = "上架前检查主图、库存、绿豆和履约信息")
	public R<MallProductEntity> changeStatus(@RequestBody Map<String, Object> body) {
		Long id = Func.toLong(body.get("id"));
		Integer status = Func.toInt(body.get("status"), 0);
		if (id == null) return R.fail("缺少商品ID");
		return R.data(operationService.changeStatus(id, status));
	}
}
