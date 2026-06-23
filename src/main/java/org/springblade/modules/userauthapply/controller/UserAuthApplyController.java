/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 * <p>
 * Use of this software is governed by the Commercial License Agreement
 * obtained after purchasing a license from BladeX.
 * <p>
 * 1. This software is for development use only under a valid license
 * from BladeX.
 * <p>
 * 2. Redistribution of this software's source code to any third party
 * without a commercial license is strictly prohibited.
 * <p>
 * 3. Licensees may copyright their own code but cannot use segments
 * from this software for such purposes. Copyright of this software
 * remains with BladeX.
 * <p>
 * Using this software signifies agreement to this License, and the software
 * must not be used for illegal purposes.
 * <p>
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY. The author is
 * not liable for any claims arising from secondary or illegal development.
 * <p>
 * Author: Chill Zhuang (bladejava@qq.com)
 */
package org.springblade.modules.userauthapply.controller;

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
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.userauthapply.excel.UserAuthApplyExcel;
import org.springblade.modules.userauthapply.pojo.dto.UserAuthApplyReviewDTO;
import org.springblade.modules.userauthapply.pojo.entity.UserAuthApplyEntity;
import org.springblade.modules.userauthapply.pojo.vo.UserAuthApplyVO;
import org.springblade.modules.userauthapply.service.IUserAuthApplyService;
import org.springblade.modules.userauthapply.wrapper.UserAuthApplyWrapper;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户认证申请表 控制器
 *
 * @author BladeX
 * @since 2026-04-02
 */
@RestController
@AllArgsConstructor
@RequestMapping("blade-userauthapply/userAuthApply")
@Tag(name = "用户认证申请表", description = "用户认证申请表接口")
public class UserAuthApplyController extends BladeController {

    private final IUserAuthApplyService userAuthApplyService;
    private final IUserService userService;

    @GetMapping("/detail")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "详情", description = "传入userAuthApply")
    public R<UserAuthApplyVO> detail(UserAuthApplyEntity userAuthApply) {
        UserAuthApplyEntity detail = userAuthApplyService.getOne(Condition.getQueryWrapper(userAuthApply));
        if (detail == null) {
            return R.data(null);
        }
        return R.data(UserAuthApplyWrapper.build().entityVO(detail));
    }

    @ApiOperationSupport(order = 2)
    @GetMapping("/list")
    @Operation(summary = "分页", description = "传入userAuthApply")
    public R<IPage<UserAuthApplyVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> userAuthApply, Query query) {
        IPage<UserAuthApplyEntity> pages = userAuthApplyService.page(Condition.getPage(query), Condition.getQueryWrapper(userAuthApply, UserAuthApplyEntity.class));
        return R.data(UserAuthApplyWrapper.build().pageVO(pages));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "分页", description = "传入userAuthApply")
    public R<IPage<UserAuthApplyVO>> page(UserAuthApplyVO userAuthApply, Query query) {
        IPage<UserAuthApplyVO> pages = userAuthApplyService.selectUserAuthApplyPage(Condition.getPage(query), userAuthApply);
        return R.data(pages);
    }

    @PostMapping("/save")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "新增", description = "传入userAuthApply")
    public R save(@Valid @RequestBody UserAuthApplyEntity userAuthApply) {
        return R.status(userAuthApplyService.save(userAuthApply));
    }

    @PostMapping("/update")
    @ApiOperationSupport(order = 5)
    @Operation(summary = "修改", description = "传入userAuthApply")
    public R update(@Valid @RequestBody UserAuthApplyEntity userAuthApply) {
        return R.status(userAuthApplyService.updateById(userAuthApply));
    }

    @PostMapping("/submit")
    @ApiOperationSupport(order = 6)
    @Operation(summary = "新增或修改", description = "传入userAuthApply")
    public R submit(@Valid @RequestBody UserAuthApplyEntity userAuthApply) {
        return R.status(userAuthApplyService.saveOrUpdate(userAuthApply));
    }

    @PostMapping("/remove")
    @ApiOperationSupport(order = 7)
    @Operation(summary = "逻辑删除", description = "传入ids")
    public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
        return R.status(userAuthApplyService.deleteLogic(Func.toLongList(ids)));
    }

    @PostMapping("/review")
    @ApiOperationSupport(order = 8)
    @Operation(summary = "审核", description = "审核申请并联动写入审核日志、用户身份")
    public R review(@Valid @RequestBody UserAuthApplyReviewDTO reviewDTO) {
        return R.status(userAuthApplyService.review(reviewDTO, AuthUtil.getUserId()));
    }

    @GetMapping("/summary")
    @ApiOperationSupport(order = 9)
    @Operation(summary = "汇总", description = "传入userId")
    public R<Map<String, Object>> summary(@RequestParam Long userId) {
        Map<String, Object> result = new LinkedHashMap<>();

        QueryWrapper<UserAuthApplyEntity> latestWrapper = new QueryWrapper<>();
        latestWrapper.eq("user_id", userId).eq("is_deleted", 0).orderByDesc("update_time").orderByDesc("create_time").orderByDesc("id");
        UserAuthApplyEntity latest = userAuthApplyService.getOne(latestWrapper, false);

        QueryWrapper<UserAuthApplyEntity> approvedWrapper = new QueryWrapper<>();
        approvedWrapper.eq("user_id", userId).eq("is_deleted", 0).eq("apply_status", 2).orderByDesc("approved_time").orderByDesc("update_time").orderByDesc("id");
        List<UserAuthApplyEntity> approvedList = userAuthApplyService.list(approvedWrapper);

        Integer authStatus = 0;
        String mainIdentityCode = "";
        String mainIdentityName = "";
        String authRefuseReason = "";
        String authApplyId = "";
        String authUpdatedAt = "";

        if (latest != null) {
            authStatus = latest.getApplyStatus() == null ? 0 : latest.getApplyStatus();
            mainIdentityCode = latest.getAuthTypeCode() == null ? "" : latest.getAuthTypeCode();
            mainIdentityName = latest.getAuthTypeName() == null ? "" : latest.getAuthTypeName();
            authRefuseReason = latest.getAuditReason() == null ? "" : latest.getAuditReason();
            authApplyId = latest.getId() == null ? "" : String.valueOf(latest.getId());
            if (latest.getUpdateTime() != null) {
                authUpdatedAt = DateUtil.format(latest.getUpdateTime(), DateUtil.PATTERN_DATETIME);
            } else if (latest.getCreateTime() != null) {
                authUpdatedAt = DateUtil.format(latest.getCreateTime(), DateUtil.PATTERN_DATETIME);
            }
        } else if (!approvedList.isEmpty()) {
            UserAuthApplyEntity approved = approvedList.get(0);
            authStatus = 2;
            mainIdentityCode = approved.getAuthTypeCode() == null ? "" : approved.getAuthTypeCode();
            mainIdentityName = approved.getAuthTypeName() == null ? "" : approved.getAuthTypeName();
            authApplyId = approved.getId() == null ? "" : String.valueOf(approved.getId());
            if (approved.getApprovedTime() != null) {
                authUpdatedAt = DateUtil.format(approved.getApprovedTime(), DateUtil.PATTERN_DATETIME);
            } else if (approved.getUpdateTime() != null) {
                authUpdatedAt = DateUtil.format(approved.getUpdateTime(), DateUtil.PATTERN_DATETIME);
            }
        }

        List<String> identityBadges = new ArrayList<>();
        for (UserAuthApplyEntity item : approvedList) {
            if (item != null && item.getAuthTypeName() != null && !identityBadges.contains(item.getAuthTypeName())) {
                identityBadges.add(item.getAuthTypeName());
            }
        }
        User user = userService.getById(userId);
        if (user != null && user.getAuthStatus() != null) {
            authStatus = user.getAuthStatus();
            if (user.getMainIdentityCode() != null && !user.getMainIdentityCode().isEmpty()) {
                mainIdentityCode = user.getMainIdentityCode();
            }
            if (user.getMainIdentityName() != null && !user.getMainIdentityName().isEmpty()) {
                mainIdentityName = user.getMainIdentityName();
            }
            if (user.getAuthRefuseReason() != null) {
                authRefuseReason = user.getAuthRefuseReason();
            }
            if (user.getIdentityBadges() != null && !user.getIdentityBadges().trim().isEmpty()) {
                identityBadges = new ArrayList<>();
                for (String badge : user.getIdentityBadges().split(",")) {
                    if (badge != null && !badge.trim().isEmpty() && !identityBadges.contains(badge.trim())) {
                        identityBadges.add(badge.trim());
                    }
                }
            }
        }
        if (identityBadges.isEmpty() && mainIdentityName != null && !mainIdentityName.isEmpty()) {
            identityBadges.add(mainIdentityName);
        }

        result.put("authStatus", authStatus);
        result.put("mainIdentityCode", mainIdentityCode);
        result.put("mainIdentityName", mainIdentityName);
        result.put("identityBadges", identityBadges);
        result.put("authRefuseReason", authRefuseReason);
        result.put("authApplyId", authApplyId);
        result.put("authUpdatedAt", authUpdatedAt);
        result.put("userId", userId);

        return R.data(result);
    }

    @IsAdmin
    @GetMapping("/export-userAuthApply")
    @ApiOperationSupport(order = 10)
    @Operation(summary = "导出数据", description = "传入userAuthApply")
    public void exportUserAuthApply(@Parameter(hidden = true) @RequestParam Map<String, Object> userAuthApply, BladeUser bladeUser, HttpServletResponse response) {
        QueryWrapper<UserAuthApplyEntity> queryWrapper = Condition.getQueryWrapper(userAuthApply, UserAuthApplyEntity.class);
        List<UserAuthApplyExcel> list = userAuthApplyService.exportUserAuthApply(queryWrapper);
        ExcelUtil.export(response, "用户认证申请表数据" + DateUtil.time(), "用户认证申请表数据表", list, UserAuthApplyExcel.class);
    }
}
