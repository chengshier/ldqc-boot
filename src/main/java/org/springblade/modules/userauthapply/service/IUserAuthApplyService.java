package org.springblade.modules.userauthapply.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.userauthapply.excel.UserAuthApplyExcel;
import org.springblade.modules.userauthapply.pojo.dto.UserAuthApplyReviewDTO;
import org.springblade.modules.userauthapply.pojo.entity.UserAuthApplyEntity;
import org.springblade.modules.userauthapply.pojo.vo.UserAuthApplyVO;

import java.util.List;

/**
 * 用户认证申请表 服务类
 *
 * @author BladeX
 * @since 2026-04-02
 */
public interface IUserAuthApplyService extends BaseService<UserAuthApplyEntity> {
    /**
     * 自定义分页
     *
     * @param page 分页参数
     * @param userAuthApply 查询参数
     * @return IPage<UserAuthApplyVO>
     */
    IPage<UserAuthApplyVO> selectUserAuthApplyPage(IPage<UserAuthApplyVO> page, UserAuthApplyVO userAuthApply);

    /**
     * 导出数据
     *
     * @param queryWrapper 查询条件
     * @return List<UserAuthApplyExcel>
     */
    List<UserAuthApplyExcel> exportUserAuthApply(Wrapper<UserAuthApplyEntity> queryWrapper);

    /**
     * 审核申请并联动写入审核日志、用户身份信息
     *
     * @param reviewDTO 审核请求
     * @param auditUserId 审核人ID
     * @return 是否成功
     */
    boolean review(UserAuthApplyReviewDTO reviewDTO, Long auditUserId);
}
