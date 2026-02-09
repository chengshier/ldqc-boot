package org.springblade.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.pojo.vo.FollowVO;
import org.springblade.modules.system.pojo.vo.TrendVO;
import org.springblade.modules.system.pojo.vo.UserRecordVO;
import org.springblade.modules.system.service.IAppUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/blade-system/app-user")
@Tag(name = "App用户扩展", description = "App用户扩展接口")
public class AppUserController {

    private final IAppUserService appUserService;

    @GetMapping("/getTrendByUser")
    @Operation(summary = "获取用户动态")
    public R<IPage<TrendVO>> getTrendByUser(@RequestParam long page, @RequestParam long limit, @RequestParam String userId, @RequestParam Integer type) {
        return R.data(appUserService.getTrendByUser(new Page<>(page, limit), userId, type));
    }

    @GetMapping("/searchUser")
    @Operation(summary = "搜索用户")
    public R<IPage<FollowVO>> searchUser(@RequestParam long page, @RequestParam long limit, @RequestParam String keyword, @RequestParam String uid) {
        return R.data(appUserService.searchUser(new Page<>(page, limit), keyword, uid));
    }

    @GetMapping("/getUserRecord")
    @Operation(summary = "获取用户数据")
    public R<UserRecordVO> getUserRecord(@RequestParam String uid) {
        return R.data(appUserService.getUserRecord(uid));
    }

    @RequestMapping("/getUserInfo")
    @Operation(summary = "获取用户信息")
    public R<User> getUserInfo(@RequestParam String uid) {
        return R.data(appUserService.getUserInfo(uid));
    }

    @PostMapping("/updateUser")
    @Operation(summary = "更新用户信息")
    public R<User> updateUser(@RequestBody User user) {
        return R.data(appUserService.updateUser(user));
    }

    @RequestMapping("/searchUserByUsername")
    @Operation(summary = "用户名精确查找")
    public R<List<FollowVO>> searchUserByUsername(@RequestParam String keyword) {
        return R.data(appUserService.searchUserByUsername(keyword));
    }

    @RequestMapping("/clearUserRecord")
    @Operation(summary = "清除用户记录")
    public R<Void> clearUserRecord(@RequestParam String uid, @RequestParam Integer type) {
        appUserService.clearUserRecord(uid, type);
        return R.status(true);
    }
}