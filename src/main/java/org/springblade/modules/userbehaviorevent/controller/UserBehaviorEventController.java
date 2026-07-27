package org.springblade.modules.userbehaviorevent.controller;

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
import org.springblade.modules.userbehaviorevent.excel.UserBehaviorEventExcel;
import org.springblade.modules.userbehaviorevent.pojo.entity.UserBehaviorEventEntity;
import org.springblade.modules.userbehaviorevent.pojo.vo.UserBehaviorEventVO;
import org.springblade.modules.userbehaviorevent.service.IUserBehaviorEventService;
import org.springblade.modules.userbehaviorevent.wrapper.UserBehaviorEventWrapper;
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
@RequestMapping("blade-userbehaviorevent/userBehaviorEvent")
@Tag(name = "用户行为事件", description = "用户行为事件接口")
public class UserBehaviorEventController extends BladeController {

    private final IUserBehaviorEventService userBehaviorEventService;

    @GetMapping("/detail")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "详情", description = "传入userBehaviorEvent")
    public R<UserBehaviorEventVO> detail(UserBehaviorEventEntity userBehaviorEvent) {
        UserBehaviorEventEntity detail = userBehaviorEventService.getOne(Condition.getQueryWrapper(userBehaviorEvent));
        return R.data(UserBehaviorEventWrapper.build().entityVO(detail));
    }

    @GetMapping("/list")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "分页", description = "传入userBehaviorEvent")
    public R<IPage<UserBehaviorEventVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> userBehaviorEvent, Query query) {
        IPage<UserBehaviorEventEntity> pages = userBehaviorEventService.page(Condition.getPage(query), Condition.getQueryWrapper(userBehaviorEvent, UserBehaviorEventEntity.class));
        return R.data(UserBehaviorEventWrapper.build().pageVO(pages));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "自定义分页", description = "传入userBehaviorEvent")
    public R<IPage<UserBehaviorEventVO>> page(UserBehaviorEventVO userBehaviorEvent, Query query) {
        IPage<UserBehaviorEventVO> pages = userBehaviorEventService.selectUserBehaviorEventPage(Condition.getPage(query), userBehaviorEvent);
        return R.data(pages);
    }

    @PostMapping("/save")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "新增", description = "传入userBehaviorEvent")
    public R save(@Valid @RequestBody UserBehaviorEventEntity userBehaviorEvent) {
        return R.status(userBehaviorEventService.save(userBehaviorEvent));
    }

    @PostMapping("/update")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "修改", description = "传入userBehaviorEvent")
    public R update(@Valid @RequestBody UserBehaviorEventEntity userBehaviorEvent) {
        return R.status(userBehaviorEventService.updateById(userBehaviorEvent));
    }

    @PostMapping("/submit")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "新增或修改", description = "传入userBehaviorEvent")
    public R submit(@Valid @RequestBody UserBehaviorEventEntity userBehaviorEvent) {
        return R.status(userBehaviorEventService.saveOrUpdate(userBehaviorEvent));
    }

    @PostMapping("/remove")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "逻辑删除", description = "传入ids")
    public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
        return R.status(userBehaviorEventService.deleteLogic(Func.toLongList(ids)));
    }

    @IsAdmin
    @GetMapping("/export-userBehaviorEvent")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "导出数据", description = "传入userBehaviorEvent")
    public void exportUserBehaviorEvent(@Parameter(hidden = true) @RequestParam Map<String, Object> userBehaviorEvent, BladeUser bladeUser, HttpServletResponse response) {
        QueryWrapper<UserBehaviorEventEntity> queryWrapper = Condition.getQueryWrapper(userBehaviorEvent, UserBehaviorEventEntity.class);
        List<UserBehaviorEventExcel> list = userBehaviorEventService.exportUserBehaviorEvent(queryWrapper);
        ExcelUtil.export(response, "用户行为事件数据" + DateUtil.time(), "用户行为事件数据表", list, UserBehaviorEventExcel.class);
    }
}
