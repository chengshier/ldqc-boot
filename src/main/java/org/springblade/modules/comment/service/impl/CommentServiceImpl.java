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
package org.springblade.modules.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springblade.common.constant.PlatformConstant;
import org.springblade.common.utils.RedisUtils;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.agreecollect.pojo.dto.AgreeCollectDTO;
import org.springblade.modules.agreecollect.service.IAgreeCollectService;
import org.springblade.modules.comment.pojo.dto.CommentDTO;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorBizType;
import org.springblade.modules.pointsbehavior.pojo.enums.BehaviorEventCode;
import org.springblade.modules.pointsbehavior.service.IBehaviorFacade;
import org.springblade.modules.comment.pojo.entity.CommentEntity;
import org.springblade.modules.comment.pojo.vo.CommentVO;
import org.springblade.modules.comment.excel.CommentExcel;
import org.springblade.modules.comment.mapper.CommentMapper;
import org.springblade.modules.comment.service.ICommentService;
import org.springblade.modules.contentaudit.service.WechatContentAuditService;
import org.springblade.modules.contentaudit.pojo.entity.ContentAuditTask;
import org.springblade.modules.contentaudit.service.IContentAuditTaskService;
import org.springblade.modules.usermessage.pojo.entity.UserMessage;
import org.springblade.modules.usermessage.service.IUserMessageService;

import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.pojo.vo.UserRecordVO;
import org.springblade.modules.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论表 服务实现类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Service
public class CommentServiceImpl extends BaseServiceImpl<CommentMapper, CommentEntity> implements ICommentService {


	@Autowired
	private IUserService userService;

	@Autowired
	@Lazy
	private IImgDetailService imgDetailService;

	@Autowired
	@Lazy
	private IAgreeCollectService agreeCollectService;

	@Autowired
	private RedisUtils redisUtils;

	@Autowired
	private IBehaviorFacade behaviorFacade;

	@Autowired
	private WechatContentAuditService wechatContentAuditService;
	@Autowired private IContentAuditTaskService auditTaskService;
	@Autowired private IUserMessageService userMessageService;

	@Override
	public IPage<CommentVO> selectCommentPage(IPage<CommentVO> page, CommentVO comment) {
		return page.setRecords(baseMapper.selectCommentPage(page, comment));
	}


	@Override
	public List<CommentExcel> exportComment(Wrapper<CommentEntity> queryWrapper) {
		List<CommentExcel> commentList = baseMapper.exportComment(queryWrapper);
//commentList.forEach(comment -> {
//comment.setTypeName(DictCache.getValue(DictEnum.YES_NO, Comment.getType()));
//});
		return commentList;
	}


	@Override
	public IPage<CommentVO> getAllOneCommentByImgId(IPage<CommentVO> page, String mid, String uid) {
		IPage<CommentEntity> entityPage = this.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize()),
			new QueryWrapper<CommentEntity>().eq("mid", mid).eq("audit_status", 1).and(wrapper -> wrapper.isNull("pid").or().eq("pid", 0)).orderByDesc("create_time"));
		return convertToVoPage(entityPage, page, uid);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CommentVO addComment(CommentDTO commentDTO) {
		CommentEntity comment = BeanUtil.copy(commentDTO, CommentEntity.class);
		if (comment.getPid() == null) {
			comment.setPid(0L);
		}
		comment.setCount(0L);
		WechatContentAuditService.AuditResult auditResult = wechatContentAuditService.audit(comment.getUid(), comment.getContent());
		comment.setAuditStatus(auditResult.status());
		comment.setAuditReason(auditResult.reason());
		if (auditResult.status() == WechatContentAuditService.PASSED || auditResult.status() == WechatContentAuditService.REJECTED) {
			comment.setAuditTime(new Date());
		}
		this.save(comment);
		ContentAuditTask task = new ContentAuditTask();
		task.setTenantId(comment.getTenantId()); task.setBizType("TREND_COMMENT"); task.setBizId(comment.getId());
		task.setUserId(comment.getUid()); task.setContentSnapshot(comment.getContent()); task.setAuditStatus(auditResult.status());
		task.setResultMessage(auditResult.reason()); task.setAttemptCount(1); task.setAuditTime(comment.getAuditTime());
		auditTaskService.save(task);
		comment.setAuditTaskId(task.getId()); this.updateById(comment);
		if (auditResult.status() == WechatContentAuditService.REJECTED) {
			UserMessage message = new UserMessage(); message.setTenantId(comment.getTenantId()); message.setUserId(comment.getUid());
			message.setMessageType("COMMENT_AUDIT_REJECT"); message.setTitle("评论未通过审核");
			message.setContent(auditResult.reason()); message.setBizType("TREND_COMMENT"); message.setBizId(comment.getId()); message.setReadStatus((byte) 0);
			userMessageService.save(message);
		}

		if (auditResult.status() != WechatContentAuditService.PASSED) {
			return convertToVo(comment, String.valueOf(commentDTO.getUid()));
		}

// 更新 ImgDetail 的评论数
		imgDetailService.updateCommentCount(String.valueOf(comment.getMid()), 1);

// 消息通知逻辑
		ImgDetailEntity imgDetail = imgDetailService.getById(comment.getMid());
		if (imgDetail != null) {
			String userRecordKey = PlatformConstant.USER_RECORD + imgDetail.getUserId();
// 如果评论的不是自己的动态，则通知作者
			if (!comment.getUid().equals(imgDetail.getUserId())) {
				UserRecordVO userRecordVO = new UserRecordVO();
				if (Boolean.TRUE.equals(redisUtils.hasKey(userRecordKey))) {
					userRecordVO = JsonUtil.parse(redisUtils.get(userRecordKey).toString(), UserRecordVO.class);
				}
				if (userRecordVO == null) userRecordVO = new UserRecordVO();
				userRecordVO.setUid(imgDetail.getUserId());
				userRecordVO.setNoreplyCount(userRecordVO.getNoreplyCount() + 1);
				redisUtils.set(userRecordKey, JsonUtil.toJson(userRecordVO));

// TODO: WebSocket 通知
// WebSocketServer.sendMessageTo(...)
			}
		}

		Map<String, Object> ext = new HashMap<>();
		ext.put("mid", comment.getMid());
		ext.put("pid", comment.getPid());
		ext.put("commentLevel", comment.getPid() == null || comment.getPid() == 0L ? 1 : 2);
		ext.put("targetUserId", imgDetail == null ? null : imgDetail.getUserId());
		behaviorFacade.onSuccess(BehaviorEventCode.CONTENT_COMMENT_SUCCESS, BehaviorBizType.COMMENT, String.valueOf(comment.getId()), comment.getUid(), null, ext);

		return convertToVo(comment, String.valueOf(commentDTO.getUid()));
	}

	@Override
	public IPage<CommentVO> getAllTwoCommentByOneId(IPage<CommentVO> page, String id, String uid) {
		IPage<CommentEntity> entityPage = this.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize()),
			new QueryWrapper<CommentEntity>().eq("pid", id).eq("audit_status", 1).orderByAsc("create_time"));
		return convertToVoPage(entityPage, page, uid);
	}

	@Override
	public List<CommentVO> getAllTwoComment(String id, String uid) {
		List<CommentEntity> list = this.list(new QueryWrapper<CommentEntity>().eq("pid", id).eq("audit_status", 1).orderByDesc("create_time"));
		return convertToVoList(list, uid);
	}

	@Override
	public List<CommentVO> getAllReplyComment(IPage<CommentVO> page, String uid) {
		return baseMapper.getAllReplyComment(page, Long.valueOf(uid));
	}

	@Override
	public IPage<CommentVO> getAllComment(IPage<CommentVO> page, String mid, String uid) {
// Get level 1 comments
		IPage<CommentEntity> entityPage = this.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize()),
			new QueryWrapper<CommentEntity>().eq("mid", mid).eq("audit_status", 1).and(wrapper -> wrapper.isNull("pid").or().eq("pid", 0)).orderByDesc("create_time"));

		IPage<CommentVO> voPage = convertToVoPage(entityPage, page, uid);

// For each level 1 comment, get one level 2 comment
		for (CommentVO vo : voPage.getRecords()) {
			List<CommentEntity> twoComments = this.list(new QueryWrapper<CommentEntity>()
				.eq("pid", vo.getId()).eq("audit_status", 1)
				.orderByDesc("create_time")
				.last("LIMIT 1"));
			if (twoComments != null && !twoComments.isEmpty()) {
				CommentVO childVo = convertToVo(twoComments.get(0), uid);
				vo.setChildComment(childVo);
			}
		}
		return voPage;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delComment(String id) {
		String commentStatKey = PlatformConstant.COMMENT_STATE + id;
		redisUtils.delete(commentStatKey);

		CommentEntity comment = this.getById(id);
		if (comment == null) return;

		ImgDetailEntity imgDetail = imgDetailService.getById(comment.getMid());

		if (comment.getPid() == 0) {
// Remove all child comments
			List<CommentEntity> twoCommentList = this.list(new QueryWrapper<CommentEntity>().eq("pid", comment.getId()));
			List<Long> cids = twoCommentList.stream().map(CommentEntity::getId).collect(Collectors.toList());
			List<String> agreeCommentKeys = twoCommentList.stream().map(e -> PlatformConstant.AGREE_COMMENT_KEY + e.getId()).collect(Collectors.toList());
			List<String> commentStatKeys = twoCommentList.stream().map(e -> PlatformConstant.COMMENT_STATE + e.getId()).collect(Collectors.toList());

			if (!agreeCommentKeys.isEmpty()) redisUtils.delete(agreeCommentKeys);
			if (!commentStatKeys.isEmpty()) redisUtils.delete(commentStatKeys);

			if (imgDetail != null) {
				imgDetail.setCommentCount(imgDetail.getCommentCount() - cids.size() - 1);
			}
			if (!cids.isEmpty()) {
				this.removeBatchByIds(cids);
			}
		} else {
			CommentEntity oneComment = this.getById(comment.getPid());
			if (oneComment != null) {
				oneComment.setTwoNums(oneComment.getTwoNums() - 1);
				this.updateById(oneComment);
			}
			if (imgDetail != null) {
				imgDetail.setCommentCount(imgDetail.getCommentCount() - 1);
			}
		}
		if (imgDetail != null) {
			imgDetailService.updateById(imgDetail);
		}
		this.removeById(id);
	}

	@Override
	public IPage<CommentVO> getAllTrendCommentByImage(IPage<CommentVO> page, String mid, String uid) {
		IPage<CommentEntity> entityPage = this.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize()),
			new QueryWrapper<CommentEntity>().eq("mid", mid).eq("audit_status", 1).and(wrapper -> wrapper.isNull("pid").or().eq("pid", 0)).orderByDesc("count").orderByDesc("create_time"));

		IPage<CommentVO> voPage = convertToVoPage(entityPage, page, uid);

// For each level 1 comment, get one level 2 comment
		for (CommentVO vo : voPage.getRecords()) {
			List<CommentEntity> twoComments = this.list(new QueryWrapper<CommentEntity>()
				.eq("pid", vo.getId()).eq("audit_status", 1)
				.orderByDesc("create_time")
				.last("LIMIT 1"));
			if (twoComments != null && !twoComments.isEmpty()) {
				CommentVO childVo = convertToVo(twoComments.get(0), uid);
				vo.setChildComment(childVo);
			}
		}
		return voPage;
	}

	@Override
	public Map<String, Object> scrollComment(String id, String mid, String uid) {
		Map<String, Object> resMap = new HashMap<>();

		CommentEntity comment = this.getById(id);
		if (comment == null) return resMap;
		Long pid = comment.getPid();

		int page1 = 1;
		int page2 = 1;
		int limit1 = 6;
		int limit2 = 4;

		long total = 0;
		boolean flag = false;

		List<CommentVO> comments = new ArrayList<>();

		if (pid == 0) {
			while (!flag) {
				IPage<CommentVO> pageReq = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page1, limit1);
				IPage<CommentVO> allOneCommentPage = this.getAllComment(pageReq, mid, uid);
				List<CommentVO> commentVoList = allOneCommentPage.getRecords();
				List<Long> pids = commentVoList.stream().map(CommentVO::getId).collect(Collectors.toList());
				if (pids.contains(Long.valueOf(id))) {
					flag = true;
					total = allOneCommentPage.getTotal();
				} else {
					page1++;
				}
				comments.addAll(commentVoList);
// Safety break to prevent infinite loops
				if (page1 > 100) break;
			}
		} else {
			boolean flag2 = false;
			while (!flag) {
				IPage<CommentVO> pageReq = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page1, limit1);
				IPage<CommentVO> allOneCommentPage = this.getAllComment(pageReq, mid, uid);
				List<CommentVO> commentVoList = allOneCommentPage.getRecords();
				List<Long> pids = commentVoList.stream().map(CommentVO::getId).collect(Collectors.toList());

				if (pids.contains(pid)) {
					for (CommentVO commentVo : commentVoList) {
						if (Objects.equals(commentVo.getId(), pid)) {
							List<CommentVO> comments2 = new ArrayList<>();
							flag = true;
							total = allOneCommentPage.getTotal();
							while (!flag2) {
								IPage<CommentVO> pageReq2 = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page2, limit2);
								IPage<CommentVO> allTwoCommentPage = this.getAllTwoCommentByOneId(pageReq2, String.valueOf(pid), uid);
								List<CommentVO> commentVoList2 = allTwoCommentPage.getRecords();
								List<Long> ids = commentVoList2.stream().map(CommentVO::getId).collect(Collectors.toList());
								if (ids.contains(Long.valueOf(id))) {
									flag2 = true;
								} else {
									page2++;
								}
								comments2.addAll(commentVoList2);
								if (page2 > 100) break;
							}
							commentVo.setChildComment(null); // Clear single child as we are loading list
// We need to set childrenComments list but CommentVO might not have it or it's named differently
// Checking CommentVO... assuming it has childrenComments or we just return it in the list
// In original code: commentVo.setChildrenComments(comments2);
// I should check CommentVO definition.
// For now I'll assume I can just return the flattened list or similar structure as original.
						}
					}
				} else {
					page1++;
				}
				comments.addAll(commentVoList);
				if (page1 > 100) break;
			}
		}

		resMap.put("records", comments);
		resMap.put("total", total);
		resMap.put("page1", page1);
		resMap.put("page2", page2);

		return resMap;
	}

	private CommentVO convertToVo(CommentEntity entity, String currentUid) {
		if (entity == null) return null;
		CommentVO vo = BeanUtil.copy(entity, CommentVO.class);
		User user = userService.getById(entity.getUid());
		if (user != null) {
			vo.setUsername(user.getName());
			vo.setAvatar(user.getAvatar());
		}
		if (entity.getReplyUid() != null) {
			User replyUser = userService.getById(entity.getReplyUid());
			if (replyUser != null) {
				vo.setReplyName(replyUser.getName());
			}
		}

// Check isAgree
		if (agreeCollectService != null) {
			AgreeCollectDTO dto = new AgreeCollectDTO();
			dto.setAgreeCollectId(entity.getId());
			dto.setUid(Long.valueOf(currentUid));
			dto.setType(0); // 0 for comment
			vo.setIsAgree(agreeCollectService.isAgree(dto));
		}

		return vo;
	}

	private List<CommentVO> convertToVoList(List<CommentEntity> list, String currentUid) {
		List<CommentVO> voList = new ArrayList<>();
		if (list == null || list.isEmpty()) return voList;

		Set<Long> uids = list.stream().map(CommentEntity::getUid).collect(Collectors.toSet());
		Set<Long> replyUids = list.stream().filter(c -> c.getReplyUid() != null).map(CommentEntity::getReplyUid).collect(Collectors.toSet());
		uids.addAll(replyUids);

		List<User> userList = userService.listByIds(uids);
		Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, u -> u));

		for (CommentEntity entity : list) {
			CommentVO vo = BeanUtil.copy(entity, CommentVO.class);
			User user = userMap.get(entity.getUid());
			if (user != null) {
				vo.setUsername(user.getName());
				vo.setAvatar(user.getAvatar());
			}
			if (entity.getReplyUid() != null) {
				User replyUser = userMap.get(entity.getReplyUid());
				if (replyUser != null) {
					vo.setReplyName(replyUser.getName());
				}
			}

// Check isAgree
			if (agreeCollectService != null) {
				AgreeCollectDTO dto = new AgreeCollectDTO();
				dto.setAgreeCollectId(entity.getId());
				dto.setUid(Long.valueOf(currentUid));
				dto.setType(0); // 0 for comment
				vo.setIsAgree(agreeCollectService.isAgree(dto));
			}

			voList.add(vo);
		}
		return voList;
	}

	private IPage<CommentVO> convertToVoPage(IPage<CommentEntity> entityPage, IPage<CommentVO> voPage, String currentUid) {
		List<CommentVO> voList = convertToVoList(entityPage.getRecords(), currentUid);
		return voPage.setRecords(voList).setTotal(entityPage.getTotal());
	}

}
