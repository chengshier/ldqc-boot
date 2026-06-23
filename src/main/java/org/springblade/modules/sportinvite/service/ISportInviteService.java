package org.springblade.modules.sportinvite.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.sportinvite.excel.SportInviteExcel;
import org.springblade.modules.sportinvite.pojo.entity.SportInviteEntity;
import org.springblade.modules.sportinvite.pojo.vo.SportInviteVO;
import org.springblade.modules.sportinviteapply.pojo.entity.SportInviteApplyEntity;
import org.springblade.modules.sportinviteapply.pojo.vo.SportInviteApplyVO;

import java.util.List;

/**
 * 运动邀约表 服务类
 *
 * @author BladeX
 * @since 2026-05-21
 */
public interface ISportInviteService extends BaseService<SportInviteEntity> {

	IPage<SportInviteVO> selectSportInvitePage(IPage<SportInviteVO> page, SportInviteVO sportInvite);

	IPage<SportInviteVO> appPage(IPage<SportInviteEntity> page, SportInviteEntity sportInvite);

	SportInviteVO appDetail(Long id);

	boolean publish(SportInviteEntity sportInvite);

	boolean cancel(Long id);

	boolean apply(SportInviteApplyEntity apply);

	IPage<SportInviteVO> myPublish(IPage<SportInviteEntity> page);

	IPage<SportInviteApplyVO> myApply(IPage<SportInviteApplyEntity> page);

	IPage<SportInviteApplyVO> applyList(IPage<SportInviteApplyEntity> page, Long inviteId, String applyStatus);

	boolean audit(Long applyId, String auditAction, String auditRemark);

	List<SportInviteExcel> exportSportInvite(Wrapper<SportInviteEntity> queryWrapper);

}
