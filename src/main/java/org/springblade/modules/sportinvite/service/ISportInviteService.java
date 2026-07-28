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
import java.util.Map;

/** 绿动有约业务服务。 */
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

	/** 管理端全局申请分页，返回申请人昵称和邀约标题。 */
	IPage<SportInviteApplyVO> adminApplyList(IPage<SportInviteApplyEntity> page, Long inviteId, String applyStatus);

	/** 管理员代处理异常或长期未处理的申请。 */
	boolean adminAudit(Long applyId, String auditAction, String auditRemark);

	/** 管理端全局待办汇总。 */
	Map<String, Long> adminSummary();

	List<SportInviteExcel> exportSportInvite(Wrapper<SportInviteEntity> queryWrapper);
}
