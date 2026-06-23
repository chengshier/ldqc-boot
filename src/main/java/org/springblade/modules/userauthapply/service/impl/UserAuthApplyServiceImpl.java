package org.springblade.modules.userauthapply.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;

import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.userauthapply.excel.UserAuthApplyExcel;
import org.springblade.modules.userauthapply.mapper.UserAuthApplyMapper;
import org.springblade.modules.userauthapply.pojo.dto.UserAuthApplyReviewDTO;
import org.springblade.modules.userauthapply.pojo.entity.UserAuthApplyEntity;
import org.springblade.modules.userauthapply.pojo.vo.UserAuthApplyVO;
import org.springblade.modules.userauthapply.service.IUserAuthApplyService;
import org.springblade.modules.userauthaudit.pojo.entity.UserAuthAuditEntity;
import org.springblade.modules.userauthaudit.service.IUserAuthAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户认证申请表 服务实现类
 *
 * @author BladeX
 * @since 2026-04-02
 */
@Service
@RequiredArgsConstructor
public class UserAuthApplyServiceImpl extends BaseServiceImpl<UserAuthApplyMapper, UserAuthApplyEntity> implements IUserAuthApplyService {

    private final IUserAuthAuditService userAuthAuditService;
    private final IUserService userService;

    @Override
    public IPage<UserAuthApplyVO> selectUserAuthApplyPage(IPage<UserAuthApplyVO> page, UserAuthApplyVO userAuthApply) {
        return page.setRecords(baseMapper.selectUserAuthApplyPage(page, userAuthApply));
    }

    @Override
    public List<UserAuthApplyExcel> exportUserAuthApply(Wrapper<UserAuthApplyEntity> queryWrapper) {
        return baseMapper.exportUserAuthApply(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean review(UserAuthApplyReviewDTO reviewDTO, Long auditUserId) {
        if (reviewDTO == null || reviewDTO.getApplyId() == null) {
            throw new ServiceException("申请记录不能为空");
        }
        if (reviewDTO.getAuditStatus() == null || (reviewDTO.getAuditStatus() != 1 && reviewDTO.getAuditStatus() != 2)) {
            throw new ServiceException("审核状态不合法");
        }
        if (reviewDTO.getAuditStatus() == 2 && (reviewDTO.getAuditOpinion() == null || reviewDTO.getAuditOpinion().trim().isEmpty())) {
            throw new ServiceException("驳回时请填写审核意见");
        }

        UserAuthApplyEntity apply = getById(reviewDTO.getApplyId());
        if (apply == null) {
            throw new ServiceException("认证申请不存在");
        }

        Date now = new Date();
        apply.setApplyStatus(reviewDTO.getAuditStatus() == 1 ? 2 : 3);
        apply.setAuditReason(reviewDTO.getAuditStatus() == 1 ? null : reviewDTO.getAuditOpinion());
        apply.setLastAuditUser(auditUserId);
        apply.setLastAuditTime(now);
        apply.setApprovedTime(reviewDTO.getAuditStatus() == 1 ? now : null);
        boolean updated = updateById(apply);

        UserAuthAuditEntity audit = new UserAuthAuditEntity();
        audit.setApplyId(apply.getId());
        audit.setUserId(apply.getUserId());
        audit.setAuthTypeCode(apply.getAuthTypeCode());
        audit.setAuditStatus(reviewDTO.getAuditStatus());
        audit.setAuditOpinion(reviewDTO.getAuditOpinion());
        audit.setAuditUser(auditUserId);
        audit.setAuditTime(now);
        boolean auditSaved = userAuthAuditService.save(audit);

        syncUserAuthProfile(apply, reviewDTO.getAuditStatus(), reviewDTO.getAuditOpinion());
        return updated && auditSaved;
    }

    private void syncUserAuthProfile(UserAuthApplyEntity apply, Integer auditStatus, String auditOpinion) {
        if (apply.getUserId() == null) {
            return;
        }
        User user = new User();
        user.setId(apply.getUserId());

        QueryWrapper<UserAuthApplyEntity> approvedWrapper = new QueryWrapper<>();
        approvedWrapper.eq("user_id", apply.getUserId())
            .eq("is_deleted", 0)
            .eq("apply_status", 2)
            .orderByDesc("approved_time")
            .orderByDesc("update_time")
            .orderByDesc("id");
        List<UserAuthApplyEntity> approvedList = list(approvedWrapper);

        Set<String> badgeSet = new LinkedHashSet<>();
        for (UserAuthApplyEntity item : approvedList) {
            if (item != null && item.getAuthTypeName() != null && !item.getAuthTypeName().trim().isEmpty()) {
                badgeSet.add(item.getAuthTypeName().trim());
            }
        }
        List<String> badgeList = new ArrayList<>(badgeSet);

        if (!approvedList.isEmpty()) {
            UserAuthApplyEntity primary = approvedList.get(0);
            if (auditStatus != null && auditStatus == 1 && apply.getAuthTypeCode() != null) {
                primary = apply;
            }
            user.setAuthStatus(2);
            user.setMainIdentityCode(primary.getAuthTypeCode());
            user.setMainIdentityName(primary.getAuthTypeName());
            user.setIdentityBadges(String.join(",", badgeList));
            user.setAuthRefuseReason(null);
        } else {
            user.setAuthStatus(auditStatus == null ? 0 : (auditStatus == 1 ? 2 : 3));
            user.setMainIdentityCode(apply.getAuthTypeCode());
            user.setMainIdentityName(apply.getAuthTypeName());
            user.setIdentityBadges("");
            user.setAuthRefuseReason(auditStatus != null && auditStatus == 2 ? auditOpinion : null);
        }

        boolean synced = userService.updateById(user);
        if (!synced) {
            UpdateWrapper<User> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", apply.getUserId())
                .set("auth_status", user.getAuthStatus())
                .set("main_identity_code", user.getMainIdentityCode())
                .set("main_identity_name", user.getMainIdentityName())
                .set("identity_badges", user.getIdentityBadges())
                .set("auth_refuse_reason", user.getAuthRefuseReason());
            userService.update(wrapper);
        }
    }
}
