package org.springblade.modules.pointsrulecondition.controller;

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
import org.springblade.modules.pointsrulecondition.excel.PointsRuleConditionExcel;
import org.springblade.modules.pointsrulecondition.pojo.entity.PointsRuleConditionEntity;
import org.springblade.modules.pointsrulecondition.pojo.vo.PointsRuleConditionVO;
import org.springblade.modules.pointsrulecondition.service.IPointsRuleConditionService;
import org.springblade.modules.pointsrulecondition.wrapper.PointsRuleConditionWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("blade-pointsrulecondition/pointsRuleCondition")
@Tag(name = "积分规则条件", description = "积分规则条件接口")
public class PointsRuleConditionController extends BladeController {

    private final IPointsRuleConditionService pointsRuleConditionService;

    @GetMapping("/detail")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "详情", description = "传入pointsRuleCondition")
    public R<PointsRuleConditionVO> detail(PointsRuleConditionEntity pointsRuleCondition) {
        PointsRuleConditionEntity detail = pointsRuleConditionService.getOne(Condition.getQueryWrapper(pointsRuleCondition));
        return R.data(PointsRuleConditionWrapper.build().entityVO(detail));
    }

    @GetMapping("/list")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "分页", description = "传入pointsRuleCondition")
    public R<IPage<PointsRuleConditionVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> pointsRuleCondition, Query query) {
        IPage<PointsRuleConditionEntity> pages = pointsRuleConditionService.page(Condition.getPage(query), Condition.getQueryWrapper(pointsRuleCondition, PointsRuleConditionEntity.class));
        return R.data(PointsRuleConditionWrapper.build().pageVO(pages));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "自定义分页", description = "传入pointsRuleCondition")
    public R<IPage<PointsRuleConditionVO>> page(PointsRuleConditionVO pointsRuleCondition, Query query) {
        IPage<PointsRuleConditionVO> pages = pointsRuleConditionService.selectPointsRuleConditionPage(Condition.getPage(query), pointsRuleCondition);
        return R.data(pages);
    }

    @PostMapping("/save")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "新增", description = "传入pointsRuleCondition")
    public R save(@Valid @RequestBody PointsRuleConditionEntity pointsRuleCondition) {
        return R.status(pointsRuleConditionService.save(pointsRuleCondition));
    }

    @PostMapping("/update")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "修改", description = "传入pointsRuleCondition")
    public R update(@Valid @RequestBody PointsRuleConditionEntity pointsRuleCondition) {
        return R.status(pointsRuleConditionService.updateById(pointsRuleCondition));
    }

    @PostMapping("/submit")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "新增或修改", description = "传入pointsRuleCondition")
    public R submit(@Valid @RequestBody PointsRuleConditionEntity pointsRuleCondition) {
        return R.status(pointsRuleConditionService.saveOrUpdate(pointsRuleCondition));
    }

    @PostMapping("/remove")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "逻辑删除", description = "传入ids")
    public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
        return R.status(pointsRuleConditionService.deleteLogic(Func.toLongList(ids)));
    }

    @IsAdmin
    @GetMapping("/export-pointsRuleCondition")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "导出数据", description = "传入pointsRuleCondition")
    public void exportPointsRuleCondition(@Parameter(hidden = true) @RequestParam Map<String, Object> pointsRuleCondition, BladeUser bladeUser, HttpServletResponse response) {
        QueryWrapper<PointsRuleConditionEntity> queryWrapper = Condition.getQueryWrapper(pointsRuleCondition, PointsRuleConditionEntity.class);
        List<PointsRuleConditionExcel> list = pointsRuleConditionService.exportPointsRuleCondition(queryWrapper);
        ExcelUtil.export(response, "积分规则条件数据" + DateUtil.time(), "积分规则条件数据表", list, PointsRuleConditionExcel.class);
    }
}
