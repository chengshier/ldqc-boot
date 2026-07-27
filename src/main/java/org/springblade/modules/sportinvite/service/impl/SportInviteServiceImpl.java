package org.springblade.modules.sportinvite.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorBizType;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorEventCode;
import org.springblade.modules.pointsbehavior.service.IBehaviorFacade;
import org.springblade.modules.sportinvite.excel.SportInviteExcel;
import org.springblade.modules.sportinvite.mapper.SportInviteMapper;
import org.springblade.modules.sportinvite.pojo.entity.SportInviteEntity;
import org.springblade.modules.sportinvite.pojo.vo.SportInviteVO;
import org.springblade.modules.sportinvite.service.ISportInviteService;
import org.springblade.modules.sportinviteauditlog.pojo.entity.SportInviteAuditLogEntity;
import org.springblade.modules.sportinviteauditlog.service.ISportInviteAuditLogService;
import org.springblade.modules.sportinviteapply.pojo.entity.SportInviteApplyEntity;
import org.springblade.modules.sportinviteapply.pojo.vo.SportInviteApplyVO;
import org.springblade.modules.sportinviteapply.service.ISportInviteApplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 运动邀约表 服务实现类
 *
 * @author BladeX
 * @since 2026-05-21
 */
@Service
@AllArgsConstructor
public class SportInviteServiceImpl extends BaseServiceImpl<SportInviteMapper, SportInviteEntity> implements ISportInviteService {

	private final ISportInviteApplyService sportInviteApplyService;
	private final ISportInviteAuditLogService sportInviteAuditLogService;
	private final IBehaviorFacade behaviorFacade;

	@Override
	public IPage<SportInviteVO> selectSportInvitePage(IPage<SportInviteVO> page, SportInviteVO sportInvite) {
		return page.setRecords(baseMapper.selectSportInvitePage(page, sportInvite));
	}

	@Override
	public IPage<SportInviteVO> appPage(IPage<SportInviteEntity> page, SportInviteEntity sportInvite) {
		IPage<SportInviteEntity> entityPage = this.page(page, Wrappers.<SportInviteEntity>lambdaQuery()
			.eq(SportInviteEntity::getIsDeleted, 0)
			.eq(Func.isNotBlank(sportInvite.getSportType()), SportInviteEntity::getSportType, sportInvite.getSportType())
			.eq(Func.isNotBlank(sportInvite.getInviteStatus()), SportInviteEntity::getInviteStatus, sportInvite.getInviteStatus())
			.eq(Func.isNotBlank(sportInvite.getLevelLimit()), SportInviteEntity::getLevelLimit, sportInvite.getLevelLimit())
			.orderByDesc(SportInviteEntity::getCreateTime));
		return entityPage.convert(item -> org.springblade.core.tool.utils.BeanUtil.copy(item, SportInviteVO.class));
	}

	@Override
	public SportInviteVO appDetail(Long id) {
		SportInviteEntity detail = this.getById(id);
		if (detail == null || Objects.equals(detail.getIsDeleted(), 1)) {
			throw new ServiceException("邀约不存在");
		}
		SportInviteVO vo = Objects.requireNonNull(org.springblade.core.tool.utils.BeanUtil.copy(detail, SportInviteVO.class));
		Long userId = AuthUtil.getUserId();
		if (Func.isNotEmpty(userId) && userId > 0) {
			SportInviteApplyEntity apply = sportInviteApplyService.getOne(Wrappers.<SportInviteApplyEntity>lambdaQuery()
				.eq(SportInviteApplyEntity::getInviteId, id)
				.eq(SportInviteApplyEntity::getApplicantUserId, userId)
				.eq(SportInviteApplyEntity::getIsDeleted, 0)
				.last("limit 1"));
			if (apply != null) {
				vo.setMyApplyStatus(apply.getApplyStatus());
				vo.setContactVisible("APPROVED".equals(apply.getApplyStatus()));
			}
			if (Objects.equals(detail.getPublisherUserId(), userId)) {
				vo.setCanAudit(true);
				vo.setContactVisible(true);
			}
		}
		return vo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean publish(SportInviteEntity sportInvite) {
		Long userId = AuthUtil.getUserId();
		if (Func.isEmpty(userId) || userId <= 0) {
			throw new ServiceException("请先登录后再发布邀约");
		}
		if (Func.isBlank(sportInvite.getTitle())) {
			throw new ServiceException("活动标题不能为空");
		}
		if (sportInvite.getStartTime() == null || sportInvite.getEndTime() == null || !sportInvite.getEndTime().after(sportInvite.getStartTime())) {
			throw new ServiceException("活动时间不正确");
		}
		sportInvite.setPublisherUserId(userId);
		if (Func.isBlank(sportInvite.getInviteStatus())) sportInvite.setInviteStatus("OPEN");
		if (sportInvite.getCurrentPeople() == null) sportInvite.setCurrentPeople(0);
		if (sportInvite.getTargetPeople() == null || sportInvite.getTargetPeople() < 1) sportInvite.setTargetPeople(1);
		if (sportInvite.getNeedAudit() == null) sportInvite.setNeedAudit(1);
		if (Func.isBlank(sportInvite.getContactVisibleRule())) sportInvite.setContactVisibleRule("APPROVED_ONLY");
		if (sportInvite.getStatus() == null) sportInvite.setStatus(1);
		boolean saved = this.saveOrUpdate(sportInvite);
		if (saved) {
			Map<String, Object> ext = new HashMap<>();
			ext.put("sportType", sportInvite.getSportType());
			ext.put("needAudit", sportInvite.getNeedAudit());
			ext.put("publisherUserId", userId);
			behaviorFacade.onSuccess(BehaviorEventCode.INVITE_PUBLISH_SUCCESS, BehaviorBizType.SPORT_INVITE, String.valueOf(sportInvite.getId()), userId, null, ext);
		}
		return saved;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean cancel(Long id) {
		SportInviteEntity invite = this.getById(id);
		Long userId = AuthUtil.getUserId();
		if (invite == null || Objects.equals(invite.getIsDeleted(), 1)) throw new ServiceException("邀约不存在");
		if (!Objects.equals(invite.getPublisherUserId(), userId)) throw new ServiceException("只能取消自己发布的邀约");
		invite.setInviteStatus("CANCELED");
		return this.updateById(invite);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean apply(SportInviteApplyEntity apply) {
		Long userId = AuthUtil.getUserId();
		if (Func.isEmpty(userId) || userId <= 0) throw new ServiceException("请先登录后再申请加入");
		SportInviteEntity invite = this.getById(apply.getInviteId());
		if (invite == null || Objects.equals(invite.getIsDeleted(), 1)) throw new ServiceException("邀约不存在");
		if (!"OPEN".equals(invite.getInviteStatus())) throw new ServiceException("当前邀约不可申请");
		if (Objects.equals(invite.getPublisherUserId(), userId)) throw new ServiceException("不能申请自己发布的邀约");
		if (invite.getTargetPeople() != null && invite.getCurrentPeople() != null && invite.getCurrentPeople() >= invite.getTargetPeople()) throw new ServiceException("邀约已满员");
		long exists = sportInviteApplyService.count(Wrappers.<SportInviteApplyEntity>lambdaQuery()
			.eq(SportInviteApplyEntity::getInviteId, apply.getInviteId())
			.eq(SportInviteApplyEntity::getApplicantUserId, userId)
			.eq(SportInviteApplyEntity::getIsDeleted, 0));
		if (exists > 0) throw new ServiceException("你已申请过该邀约");
		apply.setApplicantUserId(userId);
		apply.setApplyStatus(Objects.equals(invite.getNeedAudit(), 0) ? "APPROVED" : "PENDING");
		if (apply.getStatus() == null) apply.setStatus(1);
		boolean saved = sportInviteApplyService.save(apply);
		if (saved && "APPROVED".equals(apply.getApplyStatus())) {
			increasePeople(invite);
		}
		if (saved) {
			Map<String, Object> ext = new java.util.HashMap<>();
			ext.put("inviteId", apply.getInviteId());
			ext.put("applyStatus", apply.getApplyStatus());
			ext.put("needAudit", invite.getNeedAudit());
			ext.put("publisherUserId", invite.getPublisherUserId());
			behaviorFacade.onSuccess(BehaviorEventCode.INVITE_APPLY_SUCCESS, BehaviorBizType.SPORT_INVITE_APPLY, String.valueOf(apply.getId()), userId, null, ext);
		}
		return saved;
	}

	@Override
	public IPage<SportInviteVO> myPublish(IPage<SportInviteEntity> page) {
		Long userId = AuthUtil.getUserId();
		IPage<SportInviteEntity> entityPage = this.page(page, Wrappers.<SportInviteEntity>lambdaQuery()
			.eq(SportInviteEntity::getPublisherUserId, userId)
			.eq(SportInviteEntity::getIsDeleted, 0)
			.orderByDesc(SportInviteEntity::getCreateTime));
		return entityPage.convert(item -> org.springblade.core.tool.utils.BeanUtil.copy(item, SportInviteVO.class));
	}

	@Override
	public IPage<SportInviteApplyVO> myApply(IPage<SportInviteApplyEntity> page) {
		Long userId = AuthUtil.getUserId();
		IPage<SportInviteApplyEntity> entityPage = sportInviteApplyService.page(page, Wrappers.<SportInviteApplyEntity>lambdaQuery()
			.eq(SportInviteApplyEntity::getApplicantUserId, userId)
			.eq(SportInviteApplyEntity::getIsDeleted, 0)
			.orderByDesc(SportInviteApplyEntity::getCreateTime));
		return entityPage.convert(item -> org.springblade.core.tool.utils.BeanUtil.copy(item, SportInviteApplyVO.class));
	}

	@Override
	public IPage<SportInviteApplyVO> applyList(IPage<SportInviteApplyEntity> page, Long inviteId, String applyStatus) {
		SportInviteEntity invite = this.getById(inviteId);
		Long userId = AuthUtil.getUserId();
		if (invite == null || !Objects.equals(invite.getPublisherUserId(), userId)) throw new ServiceException("只能查看自己邀约的申请列表");
		IPage<SportInviteApplyEntity> entityPage = sportInviteApplyService.page(page, Wrappers.<SportInviteApplyEntity>lambdaQuery()
			.eq(SportInviteApplyEntity::getInviteId, inviteId)
			.eq(Func.isNotBlank(applyStatus), SportInviteApplyEntity::getApplyStatus, applyStatus)
			.eq(SportInviteApplyEntity::getIsDeleted, 0)
			.orderByDesc(SportInviteApplyEntity::getCreateTime));
		return entityPage.convert(item -> org.springblade.core.tool.utils.BeanUtil.copy(item, SportInviteApplyVO.class));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean audit(Long applyId, String auditAction, String auditRemark) {
		SportInviteApplyEntity apply = sportInviteApplyService.getById(applyId);
		if (apply == null || Objects.equals(apply.getIsDeleted(), 1)) throw new ServiceException("申请记录不存在");
		SportInviteEntity invite = this.getById(apply.getInviteId());
		Long userId = AuthUtil.getUserId();
		if (invite == null || !Objects.equals(invite.getPublisherUserId(), userId)) throw new ServiceException("只能审核自己邀约的申请");
		if (!"PENDING".equals(apply.getApplyStatus())) throw new ServiceException("只能审核待审核申请");
		if ("APPROVE".equals(auditAction)) {
			if (invite.getTargetPeople() != null && invite.getCurrentPeople() != null && invite.getCurrentPeople() >= invite.getTargetPeople()) throw new ServiceException("邀约已满员");
			apply.setApplyStatus("APPROVED");
			increasePeople(invite);
		} else if ("REJECT".equals(auditAction)) {
			apply.setApplyStatus("REJECTED");
			apply.setRejectReason(auditRemark);
		} else {
			throw new ServiceException("审核动作不正确");
		}
		apply.setAuditUserId(userId);
		apply.setAuditTime(new Date());
		boolean updated = sportInviteApplyService.updateById(apply);
		SportInviteAuditLogEntity log = new SportInviteAuditLogEntity();
		log.setInviteId(apply.getInviteId());
		log.setApplyId(applyId);
		log.setAuditUserId(userId);
		log.setAuditAction(auditAction);
		log.setAuditRemark(auditRemark);
		log.setStatus(1);
		sportInviteAuditLogService.save(log);
		if (updated && "APPROVE".equals(auditAction)) {
			Map<String, Object> ext = new HashMap<>();
			ext.put("inviteId", apply.getInviteId());
			ext.put("applyId", apply.getId());
			ext.put("auditUserId", userId);
			ext.put("publisherUserId", invite.getPublisherUserId());
			behaviorFacade.onSuccess(BehaviorEventCode.INVITE_APPLY_APPROVED, BehaviorBizType.SPORT_INVITE_APPLY, String.valueOf(apply.getId()), apply.getApplicantUserId(), null, ext);
		}
		return updated;
	}

	@Override
	public List<SportInviteExcel> exportSportInvite(Wrapper<SportInviteEntity> queryWrapper) {
		return baseMapper.exportSportInvite(queryWrapper);
	}

	private void increasePeople(SportInviteEntity invite) {
		int current = invite.getCurrentPeople() == null ? 0 : invite.getCurrentPeople();
		int target = invite.getTargetPeople() == null ? 1 : invite.getTargetPeople();
		invite.setCurrentPeople(current + 1);
		if (invite.getCurrentPeople() >= target) invite.setInviteStatus("FULL");
		this.updateById(invite);
	}
}

