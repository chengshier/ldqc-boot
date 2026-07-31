package org.springblade.modules.sportinvite.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
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
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 绿动有约服务实现。
 *
 * <p>公开列表与未通过申请人的详情不返回完整联系方式；审核通过或免审申请
 * 通过条件更新原子占用名额，避免并发审核造成超员。</p>
 */
@Service
@AllArgsConstructor
public class SportInviteServiceImpl extends BaseServiceImpl<SportInviteMapper, SportInviteEntity> implements ISportInviteService {

	private final ISportInviteApplyService sportInviteApplyService;
	private final ISportInviteAuditLogService sportInviteAuditLogService;
	private final IBehaviorFacade behaviorFacade;
	private final IUserService userService;

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
		return entityPage.convert(this::toPublicVO);
	}

	@Override
	public SportInviteVO appDetail(Long id) {
		SportInviteEntity detail = this.getById(id);
		if (detail == null || Objects.equals(detail.getIsDeleted(), 1)) {
			throw new ServiceException("邀约不存在");
		}

		SportInviteVO vo = Objects.requireNonNull(BeanUtil.copy(detail, SportInviteVO.class));
		Long userId = AuthUtil.getUserId();
		boolean contactVisible = false;
		if (Func.isNotEmpty(userId) && userId > 0) {
			SportInviteApplyEntity apply = sportInviteApplyService.getOne(Wrappers.<SportInviteApplyEntity>lambdaQuery()
				.eq(SportInviteApplyEntity::getInviteId, id)
				.eq(SportInviteApplyEntity::getApplicantUserId, userId)
				.eq(SportInviteApplyEntity::getIsDeleted, 0)
				.last("limit 1"));
			if (apply != null) {
				vo.setMyApplyStatus(apply.getApplyStatus());
				contactVisible = "APPROVED".equalsIgnoreCase(apply.getApplyStatus());
			}
			if (Objects.equals(detail.getPublisherUserId(), userId)) {
				vo.setCanAudit(true);
				contactVisible = true;
			}
		}
		vo.setContactVisible(contactVisible);
		if (!contactVisible) clearContact(vo);
		return vo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean publish(SportInviteEntity sportInvite) {
		Long userId = AuthUtil.getUserId();
		if (Func.isEmpty(userId) || userId <= 0) throw new ServiceException("请先登录后再发布邀约");
		if (Func.isBlank(sportInvite.getTitle())) throw new ServiceException("活动标题不能为空");
		if (sportInvite.getStartTime() == null || sportInvite.getEndTime() == null || !sportInvite.getEndTime().after(sportInvite.getStartTime())) {
			throw new ServiceException("活动时间不正确");
		}
		if (sportInvite.getStartTime().before(new Date())) throw new ServiceException("活动开始时间不能早于当前时间");

		sportInvite.setId(null);
		sportInvite.setPublisherUserId(userId);
		sportInvite.setCurrentPeople(0);
		sportInvite.setInviteStatus("OPEN");
		if (sportInvite.getTargetPeople() == null || sportInvite.getTargetPeople() < 1) sportInvite.setTargetPeople(1);
		if (sportInvite.getNeedAudit() == null) sportInvite.setNeedAudit(1);
		if (Func.isBlank(sportInvite.getContactVisibleRule())) sportInvite.setContactVisibleRule("APPROVED_ONLY");
		if (sportInvite.getStatus() == null) sportInvite.setStatus(1);

		boolean saved = this.save(sportInvite);
		if (saved) {
			Map<String, Object> ext = new HashMap<>();
			ext.put("sportType", sportInvite.getSportType());
			ext.put("needAudit", sportInvite.getNeedAudit());
			ext.put("publisherUserId", userId);
			behaviorFacade.onSuccess(BehaviorEventCode.INVITE_PUBLISH_SUCCESS, BehaviorBizType.SPORT_INVITE,
				String.valueOf(sportInvite.getId()), userId, null, ext);
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
		if ("ENDED".equalsIgnoreCase(invite.getInviteStatus()) || "CANCELED".equalsIgnoreCase(invite.getInviteStatus())) {
			throw new ServiceException("当前状态不能取消");
		}
		invite.setInviteStatus("CANCELED");
		return this.updateById(invite);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean apply(SportInviteApplyEntity apply) {
		Long userId = AuthUtil.getUserId();
		if (Func.isEmpty(userId) || userId <= 0) throw new ServiceException("请先登录后再申请加入");
		if (apply == null || apply.getInviteId() == null) throw new ServiceException("缺少邀约信息");
		SportInviteEntity invite = this.getById(apply.getInviteId());
		if (invite == null || Objects.equals(invite.getIsDeleted(), 1)) throw new ServiceException("邀约不存在");
		if (!"OPEN".equalsIgnoreCase(invite.getInviteStatus())) throw new ServiceException("当前邀约不可申请");
		if (invite.getStartTime() != null && !invite.getStartTime().after(new Date())) throw new ServiceException("活动已开始，不能继续申请");
		if (Objects.equals(invite.getPublisherUserId(), userId)) throw new ServiceException("不能申请自己发布的邀约");
		long exists = sportInviteApplyService.count(Wrappers.<SportInviteApplyEntity>lambdaQuery()
			.eq(SportInviteApplyEntity::getInviteId, apply.getInviteId())
			.eq(SportInviteApplyEntity::getApplicantUserId, userId)
			.eq(SportInviteApplyEntity::getIsDeleted, 0));
		if (exists > 0) throw new ServiceException("你已申请过该邀约");

		boolean autoApprove = Objects.equals(invite.getNeedAudit(), 0);
		if (autoApprove) occupySeatOrThrow(invite.getId());
		apply.setId(null);
		apply.setApplicantUserId(userId);
		apply.setApplyStatus(autoApprove ? "APPROVED" : "PENDING");
		if (apply.getStatus() == null) apply.setStatus(1);
		boolean saved = sportInviteApplyService.save(apply);
		if (saved) {
			Map<String, Object> ext = new HashMap<>();
			ext.put("inviteId", apply.getInviteId());
			ext.put("applyStatus", apply.getApplyStatus());
			ext.put("needAudit", invite.getNeedAudit());
			ext.put("publisherUserId", invite.getPublisherUserId());
			behaviorFacade.onSuccess(BehaviorEventCode.INVITE_APPLY_SUCCESS, BehaviorBizType.SPORT_INVITE_APPLY,
				String.valueOf(apply.getId()), userId, null, ext);
			if (autoApprove) {
				behaviorFacade.onSuccess(BehaviorEventCode.INVITE_APPLY_APPROVED, BehaviorBizType.SPORT_INVITE_APPLY,
					String.valueOf(apply.getId()), userId, null, ext);
			}
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
		return entityPage.convert(item -> BeanUtil.copy(item, SportInviteVO.class));
	}

	@Override
	public IPage<SportInviteApplyVO> myApply(IPage<SportInviteApplyEntity> page) {
		Long userId = AuthUtil.getUserId();
		IPage<SportInviteApplyEntity> entityPage = sportInviteApplyService.page(page,
			Wrappers.<SportInviteApplyEntity>lambdaQuery()
				.eq(SportInviteApplyEntity::getApplicantUserId, userId)
				.eq(SportInviteApplyEntity::getIsDeleted, 0)
				.orderByDesc(SportInviteApplyEntity::getCreateTime));
		return entityPage.convert(item -> BeanUtil.copy(item, SportInviteApplyVO.class));
	}

	@Override
	public IPage<SportInviteApplyVO> applyList(IPage<SportInviteApplyEntity> page, Long inviteId, String applyStatus) {
		SportInviteEntity invite = this.getById(inviteId);
		Long userId = AuthUtil.getUserId();
		if (invite == null || !Objects.equals(invite.getPublisherUserId(), userId)) {
			throw new ServiceException("只能查看自己邀约的申请列表");
		}
		IPage<SportInviteApplyEntity> entityPage = sportInviteApplyService.page(page,
			Wrappers.<SportInviteApplyEntity>lambdaQuery()
				.eq(SportInviteApplyEntity::getInviteId, inviteId)
				.eq(Func.isNotBlank(applyStatus), SportInviteApplyEntity::getApplyStatus, applyStatus)
				.eq(SportInviteApplyEntity::getIsDeleted, 0)
				.orderByDesc(SportInviteApplyEntity::getCreateTime));
		return entityPage.convert(item -> BeanUtil.copy(item, SportInviteApplyVO.class));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean audit(Long applyId, String auditAction, String auditRemark) {
		return auditInternal(applyId, auditAction, auditRemark, false);
	}

	@Override
	public IPage<SportInviteApplyVO> adminApplyList(IPage<SportInviteApplyEntity> page, Long inviteId, String applyStatus) {
		IPage<SportInviteApplyEntity> entityPage = sportInviteApplyService.page(page,
			Wrappers.<SportInviteApplyEntity>lambdaQuery()
				.eq(inviteId != null, SportInviteApplyEntity::getInviteId, inviteId)
				.eq(Func.isNotBlank(applyStatus), SportInviteApplyEntity::getApplyStatus, applyStatus)
				.eq(SportInviteApplyEntity::getIsDeleted, 0)
				.orderByAsc(SportInviteApplyEntity::getApplyStatus)
				.orderByDesc(SportInviteApplyEntity::getCreateTime));

		List<Long> applicantIds = entityPage.getRecords().stream()
			.map(SportInviteApplyEntity::getApplicantUserId).filter(Objects::nonNull).distinct().toList();
		Map<Long, User> users = applicantIds.isEmpty() ? Map.of() : userService.listByIds(applicantIds).stream()
			.collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));
		List<Long> inviteIds = entityPage.getRecords().stream()
			.map(SportInviteApplyEntity::getInviteId).filter(Objects::nonNull).distinct().toList();
		Map<Long, SportInviteEntity> invites = inviteIds.isEmpty() ? Map.of() : this.listByIds(inviteIds).stream()
			.collect(Collectors.toMap(SportInviteEntity::getId, Function.identity(), (left, right) -> left));

		return entityPage.convert(item -> {
			SportInviteApplyVO vo = BeanUtil.copy(item, SportInviteApplyVO.class);
			if (vo == null) return null;
			User applicant = users.get(item.getApplicantUserId());
			if (applicant != null) {
				vo.setApplicantName(Func.isNotBlank(applicant.getName()) ? applicant.getName() : applicant.getRealName());
				vo.setApplicantAvatar(applicant.getAvatar());
			}
			SportInviteEntity invite = invites.get(item.getInviteId());
			if (invite != null) vo.setInviteTitle(invite.getTitle());
			return vo;
		});
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean adminAudit(Long applyId, String auditAction, String auditRemark) {
		return auditInternal(applyId, auditAction, auditRemark, true);
	}

	@Override
	public Map<String, Long> adminSummary() {
		Map<String, Long> result = new LinkedHashMap<>();
		result.put("inviteTotal", this.count(Wrappers.<SportInviteEntity>lambdaQuery().eq(SportInviteEntity::getIsDeleted, 0)));
		result.put("openInviteCount", this.count(Wrappers.<SportInviteEntity>lambdaQuery()
			.eq(SportInviteEntity::getInviteStatus, "OPEN").eq(SportInviteEntity::getIsDeleted, 0)));
		result.put("fullInviteCount", this.count(Wrappers.<SportInviteEntity>lambdaQuery()
			.eq(SportInviteEntity::getInviteStatus, "FULL").eq(SportInviteEntity::getIsDeleted, 0)));
		result.put("pendingApplyCount", sportInviteApplyService.count(Wrappers.<SportInviteApplyEntity>lambdaQuery()
			.eq(SportInviteApplyEntity::getApplyStatus, "PENDING").eq(SportInviteApplyEntity::getIsDeleted, 0)));
		return result;
	}

	private boolean auditInternal(Long applyId, String auditAction, String auditRemark, boolean administrator) {
		SportInviteApplyEntity apply = sportInviteApplyService.getById(applyId);
		if (apply == null || Objects.equals(apply.getIsDeleted(), 1)) throw new ServiceException("申请记录不存在");
		SportInviteEntity invite = this.getById(apply.getInviteId());
		Long userId = AuthUtil.getUserId();
		if (invite == null) throw new ServiceException("邀约不存在");
		if (!administrator && !Objects.equals(invite.getPublisherUserId(), userId)) {
			throw new ServiceException("只能审核自己邀约的申请");
		}
		if (!"PENDING".equalsIgnoreCase(apply.getApplyStatus())) throw new ServiceException("只能审核待审核申请");

		String action = Func.toStr(auditAction, "").trim().toUpperCase();
		String nextStatus;
		String rejectReason = null;
		if ("APPROVE".equals(action)) {
			occupySeatOrThrow(invite.getId());
			nextStatus = "APPROVED";
		} else if ("REJECT".equals(action)) {
			nextStatus = "REJECTED";
			rejectReason = auditRemark;
		} else {
			throw new ServiceException("审核动作不正确");
		}

		Date auditTime = new Date();
		boolean updated = sportInviteApplyService.update(Wrappers.<SportInviteApplyEntity>lambdaUpdate()
			.eq(SportInviteApplyEntity::getId, applyId)
			.eq(SportInviteApplyEntity::getApplyStatus, "PENDING")
			.set(SportInviteApplyEntity::getApplyStatus, nextStatus)
			.set(SportInviteApplyEntity::getRejectReason, rejectReason)
			.set(SportInviteApplyEntity::getAuditUserId, userId)
			.set(SportInviteApplyEntity::getAuditTime, auditTime));
		if (!updated) throw new ServiceException("申请状态已发生变化，请刷新后重试");

		SportInviteAuditLogEntity log = new SportInviteAuditLogEntity();
		log.setInviteId(apply.getInviteId());
		log.setApplyId(applyId);
		log.setAuditUserId(userId);
		log.setAuditAction(action);
		log.setAuditRemark((administrator ? "[平台代处理] " : "") + Func.toStr(auditRemark, ""));
		log.setStatus(1);
		sportInviteAuditLogService.save(log);

		if ("APPROVE".equals(action)) {
			Map<String, Object> ext = new HashMap<>();
			ext.put("inviteId", apply.getInviteId());
			ext.put("applyId", apply.getId());
			ext.put("auditUserId", userId);
			ext.put("publisherUserId", invite.getPublisherUserId());
			ext.put("administrator", administrator);
			behaviorFacade.onSuccess(BehaviorEventCode.INVITE_APPLY_APPROVED, BehaviorBizType.SPORT_INVITE_APPLY,
				String.valueOf(apply.getId()), apply.getApplicantUserId(), null, ext);
		}
		return true;
	}

	@Override
	public List<SportInviteExcel> exportSportInvite(Wrapper<SportInviteEntity> queryWrapper) {
		return baseMapper.exportSportInvite(queryWrapper);
	}

	/** 条件更新原子占用一个名额。 */
	private void occupySeatOrThrow(Long inviteId) {
		LambdaUpdateWrapper<SportInviteEntity> update = Wrappers.<SportInviteEntity>lambdaUpdate()
			.eq(SportInviteEntity::getId, inviteId)
			.eq(SportInviteEntity::getIsDeleted, 0)
			.eq(SportInviteEntity::getInviteStatus, "OPEN")
			.apply("COALESCE(current_people, 0) < COALESCE(target_people, 1)")
			.setSql("invite_status = CASE WHEN COALESCE(current_people, 0) + 1 >= COALESCE(target_people, 1) THEN 'FULL' ELSE 'OPEN' END")
			.setSql("current_people = COALESCE(current_people, 0) + 1");
		if (!this.update(update)) throw new ServiceException("邀约已满员或当前状态不可加入");
	}

	private SportInviteVO toPublicVO(SportInviteEntity item) {
		SportInviteVO vo = BeanUtil.copy(item, SportInviteVO.class);
		if (vo != null) {
			vo.setContactVisible(false);
			clearContact(vo);
		}
		return vo;
	}

	private void clearContact(SportInviteVO vo) {
		vo.setContactPhone(null);
		vo.setContactWechat(null);
	}
}
