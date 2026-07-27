package org.springblade.modules.banneritem.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import lombok.AllArgsConstructor;
import jakarta.validation.Valid;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
//import org.springblade.core.tool.api.ServiceException;
import org.springblade.core.tool.utils.Func;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.banneritem.pojo.entity.BannerItemEntity;
import org.springblade.modules.banneritem.pojo.vo.BannerItemVO;
import org.springblade.modules.banneritem.excel.BannerItemExcel;
import org.springblade.modules.banneritem.wrapper.BannerItemWrapper;
import org.springblade.modules.banneritem.service.IBannerItemService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.excel.util.ExcelUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@AllArgsConstructor
@RequestMapping("blade-banneritem/bannerItem")
@Tag(name = "宣传Banner内容表", description = "宣传Banner内容表接口")
public class BannerItemController extends BladeController {

	private final IBannerItemService bannerItemService;

	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description = "传入bannerItem")
	public R<BannerItemVO> detail(BannerItemEntity bannerItem) {
		BannerItemEntity detail = bannerItemService.getOne(Condition.getQueryWrapper(bannerItem));
		return R.data(BannerItemWrapper.build().entityVO(detail));
	}

	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description = "传入bannerItem")
	public R<IPage<BannerItemVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> bannerItem, Query query) {
		IPage<BannerItemEntity> pages = bannerItemService.page(Condition.getPage(query), Condition.getQueryWrapper(bannerItem, BannerItemEntity.class));
		return R.data(BannerItemWrapper.build().pageVO(pages));
	}

	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "分页", description = "传入bannerItem")
	public R<IPage<BannerItemVO>> page(BannerItemVO bannerItem, Query query) {
		IPage<BannerItemVO> pages = bannerItemService.selectBannerItemPage(Condition.getPage(query), bannerItem);
		return R.data(pages);
	}

	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "新增", description = "传入bannerItem")
	public R save(@Valid @RequestBody BannerItemEntity bannerItem) {
		normalizeExtJson(bannerItem);
		return R.status(bannerItemService.save(bannerItem));
	}

	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "修改", description = "传入bannerItem")
	public R update(@Valid @RequestBody BannerItemEntity bannerItem) {
		normalizeExtJson(bannerItem);
		return R.status(bannerItemService.updateById(bannerItem));
	}

	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "新增或修改", description = "传入bannerItem")
	public R submit(@Valid @RequestBody BannerItemEntity bannerItem) {
		normalizeExtJson(bannerItem);
		return R.status(bannerItemService.saveOrUpdate(bannerItem));
	}

	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "逻辑删除", description = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(bannerItemService.deleteLogic(Func.toLongList(ids)));
	}

	@GetMapping("/mobile/list-by-codes")
	@ApiOperationSupport(order = 20)
	@Operation(summary = "移动端按位置编码获取Banner", description = "传入codes，多个位置编码用英文逗号分隔")
	public R<Map<String, List<BannerItemVO>>> mobileListByCodes(@RequestParam String codes,
	                                                           @RequestParam(required = false, defaultValue = "miniapp") String terminal) {
		List<String> codeList = Arrays.stream(codes.split(","))
			.map(String::trim)
			.filter(code -> !code.isEmpty())
			.distinct()
			.collect(Collectors.toList());
		LinkedHashMap<String, List<BannerItemVO>> result = new LinkedHashMap<>();
		codeList.forEach(code -> result.put(code, new ArrayList<>()));
		if (codeList.isEmpty()) {
			return R.data(result);
		}
		List<BannerItemVO> bannerItems = bannerItemService.listActiveByPositionCodes(codeList, terminal);
		for (BannerItemVO bannerItem : bannerItems) {
			if (result.containsKey(bannerItem.getPositionCode())) {
				result.get(bannerItem.getPositionCode()).add(bannerItem);
			}
		}
		return R.data(result);
	}

	@IsAdmin
	@GetMapping("/export-bannerItem")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "导出数据", description = "传入bannerItem")
	public void exportBannerItem(@Parameter(hidden = true) @RequestParam Map<String, Object> bannerItem, BladeUser bladeUser, HttpServletResponse response) {
		QueryWrapper<BannerItemEntity> queryWrapper = Condition.getQueryWrapper(bannerItem, BannerItemEntity.class);
		List<BannerItemExcel> list = bannerItemService.exportBannerItem(queryWrapper);
		ExcelUtil.export(response, "宣传Banner内容表数据" + DateUtil.time(), "宣传Banner内容表数据表", list, BannerItemExcel.class);
	}
	private void normalizeExtJson(BannerItemEntity bannerItem) {
		if (bannerItem == null) {
			return;
		}
		String extJson = bannerItem.getExtJson();
		if (Func.isBlank(extJson)) {
			bannerItem.setExtJson(null);
			return;
		}
		extJson = extJson.trim();
		if (Func.isBlank(extJson)) {
			bannerItem.setExtJson(null);
			return;
		}
		try {
			JSON.parseObject(extJson);
			bannerItem.setExtJson(extJson);
		} catch (Exception ex) {
			throw new ServiceException("扩展JSON格式不正确，请输入合法的JSON对象");
		}
	}
}



