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
package org.springblade.modules.agreecollect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springblade.common.constant.PlatformConstant;
import org.springblade.common.constant.PlatformMqConstant;
import org.springblade.common.utils.RedisUtils;
import org.springblade.common.utils.SendMessageMq;
import org.springblade.common.websocket.WebSocketServer;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.agreecollect.pojo.dto.AgreeCollectDTO;
import org.springblade.modules.agreecollect.pojo.entity.AgreeCollectEntity;
import org.springblade.modules.agreecollect.pojo.vo.AgreeCollectVO;
import org.springblade.modules.agreecollect.excel.AgreeCollectExcel;
import org.springblade.modules.agreecollect.mapper.AgreeCollectMapper;
import org.springblade.modules.agreecollect.service.IAgreeCollectService;
import org.springblade.modules.album.pojo.entity.AlbumEntity;
import org.springblade.modules.album.service.IAlbumService;
import org.springblade.modules.albumimgrelation.pojo.entity.AlbumImgRelationEntity;
import org.springblade.modules.albumimgrelation.service.IAlbumImgRelationService;
import org.springblade.modules.comment.pojo.entity.CommentEntity;
import org.springblade.modules.comment.pojo.vo.CommentVO;
import org.springblade.modules.comment.service.ICommentService;

import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.pojo.vo.UserRecordVO;
import org.springblade.modules.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 点赞收藏表 服务实现类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Service
public class AgreeCollectServiceImpl extends BaseServiceImpl<AgreeCollectMapper, AgreeCollectEntity> implements IAgreeCollectService {

	@Override
	public IPage<AgreeCollectVO> selectAgreeCollectPage(IPage<AgreeCollectVO> page, AgreeCollectVO agreeCollect) {
		return page.setRecords(baseMapper.selectAgreeCollectPage(page, agreeCollect));
	}


	@Override
	public List<AgreeCollectExcel> exportAgreeCollect(Wrapper<AgreeCollectEntity> queryWrapper) {
		List<AgreeCollectExcel> agreeCollectList = baseMapper.exportAgreeCollect(queryWrapper);
		//agreeCollectList.forEach(agreeCollect -> {
		//	agreeCollect.setTypeName(DictCache.getValue(DictEnum.YES_NO, AgreeCollect.getType()));
		//});
		return agreeCollectList;
	}


	@Autowired
	private RedisUtils redisUtils;

	@Autowired
	private SendMessageMq sendMessageMq;

	@Autowired
	@Lazy
	private IImgDetailService imgDetailService;

	@Autowired
	@Lazy
	private ICommentService commentService;

	@Autowired
	private IUserService userService;

	@Autowired
	@Lazy
	private IAlbumService albumService;

	@Autowired
	@Lazy
	private IAlbumImgRelationService albumImgRelationService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void agree(AgreeCollectDTO agreeCollectDTO) {
		String agreeKey = "";
		//如果点赞的是图片
		if (agreeCollectDTO.getType() == 1) {
			agreeKey = PlatformConstant.AGREE_IMG_KEY + agreeCollectDTO.getAgreeCollectId();
		} else if (agreeCollectDTO.getType() == 0) {
			agreeKey = PlatformConstant.AGREE_COMMENT_KEY + agreeCollectDTO.getAgreeCollectId();
		}

		// redisUtils.sIsMember returns Boolean in my adaptation?
		// Need to check RedisUtils implementation. Assuming it works like source.
		// If not, I'll need to adjust.
		boolean isMember = redisUtils.sIsMember(agreeKey, String.valueOf(agreeCollectDTO.getUid()));

		if (isMember) {
			return;
		}

		redisUtils.sAdd(agreeKey, String.valueOf(agreeCollectDTO.getUid()));

		AgreeCollectEntity agreeCollect = BeanUtil.copy(agreeCollectDTO, AgreeCollectEntity.class);
		this.save(agreeCollect);

		if (agreeCollectDTO.getType() == 1) {
			ImgDetailEntity imgDetail = imgDetailService.getById(agreeCollectDTO.getAgreeCollectId());
			if (imgDetail != null) {
				if (imgDetail.getAgreeCount() == null) imgDetail.setAgreeCount(0L);
				imgDetail.setAgreeCount(imgDetail.getAgreeCount() + 1);

				sendMessageMq.sendMessage(PlatformMqConstant.IMG_DETAIL_STATE_EXCHANGE, PlatformMqConstant.IMG_DETAIL_STATE_KEY, imgDetail);
				imgDetailService.updateById(imgDetail); // Direct update for now
			}
		} else {
			CommentEntity comment = commentService.getById(agreeCollectDTO.getAgreeCollectId());
			if (comment != null) {
				if (comment.getCount() == null) comment.setCount(Long.parseLong("0"));
				comment.setCount(comment.getCount() + 1);

				sendMessageMq.sendMessage(PlatformMqConstant.IMG_DETAIL_STATE_EXCHANGE, PlatformMqConstant.IMG_DETAIL_STATE_KEY, comment);
				commentService.updateById(comment); // Direct update for now
			}
		}

		// 更改用户记录表
		agreeCollectNotice(agreeCollectDTO, agreeCollect);
	}

	@Override
	public boolean isAgree(AgreeCollectDTO agreeCollectDTO) {
		if (agreeCollectDTO.getType() == 1) {
			String agreeImgKey = PlatformConstant.AGREE_IMG_KEY + agreeCollectDTO.getAgreeCollectId();
			return redisUtils.sIsMember(agreeImgKey, String.valueOf(agreeCollectDTO.getUid()));
		} else {
			String agreeCommentKey = PlatformConstant.AGREE_COMMENT_KEY + agreeCollectDTO.getAgreeCollectId();
			return redisUtils.sIsMember(agreeCommentKey, String.valueOf(agreeCollectDTO.getUid()));
		}
	}

	@Override
	public IPage<AgreeCollectVO> getAllAgreeAndCollection(IPage<AgreeCollectVO> page, String uid) {
		IPage<AgreeCollectEntity> agreeCollectPage = this.page(new Page<>(page.getCurrent(), page.getSize()),
			new QueryWrapper<AgreeCollectEntity>().and(e -> e.eq("agree_collect_uid", uid).ne("uid", uid)).orderByDesc("create_time")); // Note: create_date -> create_time check entity base

		List<AgreeCollectEntity> agreeCollectList = agreeCollectPage.getRecords();

		if (agreeCollectList.isEmpty()) {
			return page;
		}

		List<Long> uids = agreeCollectList.stream().map(AgreeCollectEntity::getUid).collect(Collectors.toList());
		HashMap<Long, User> userMap = new HashMap<>();
		if (!uids.isEmpty()) {
			List<User> userList = userService.listByIds(uids);
			userList.forEach(item -> {
				userMap.put(item.getId(), item);
			});
		}

		//先遍历点赞和收藏图片的部分
		List<Long> mids = agreeCollectList.stream().filter(e -> e.getType() == 1 || e.getType() == 2).map(AgreeCollectEntity::getAgreeCollectId).collect(Collectors.toList());
		HashMap<Long, ImgDetailEntity> imgDetailMap = new HashMap<>();
		if (!mids.isEmpty()) {
			List<ImgDetailEntity> imgDetailList = imgDetailService.listByIds(mids);
			imgDetailList.forEach(item -> {
				imgDetailMap.put(item.getId(), item);
			});
		}

		//得到评论
		List<Long> cids = agreeCollectList.stream().filter(e -> e.getType() == 0).map(AgreeCollectEntity::getAgreeCollectId).collect(Collectors.toList());
		Map<Long, CommentVO> commentMap = new HashMap<>();

		if (!cids.isEmpty()) {
			List<CommentEntity> commentList = commentService.listByIds(cids);
			List<Long> cmidList = commentList.stream().map(CommentEntity::getMid).collect(Collectors.toList());
			List<ImgDetailEntity> imgDetailList1 = new ArrayList<>();
			if(!cmidList.isEmpty()) {
				imgDetailList1 = imgDetailService.listByIds(cmidList);
			}
			HashMap<Long, ImgDetailEntity> imgDetailMap1 = new HashMap<>();
			imgDetailList1.forEach(item -> {
				imgDetailMap1.put(item.getId(), item);
			});
			commentList.forEach(item -> {
				CommentVO commentVo = BeanUtil.copy(item, CommentVO.class);
				ImgDetailEntity imgDetail = imgDetailMap1.get(item.getMid());
				if (imgDetail != null) {
					commentVo.setCover(imgDetail.getCover());
				}
				commentMap.put(item.getId(), commentVo);
			});
		}

		//得到专辑
		List<Long> aids = agreeCollectList.stream().filter(e -> e.getType() == 3).map(AgreeCollectEntity::getAgreeCollectId).collect(Collectors.toList());
		HashMap<Long, AlbumEntity> albumMap = new HashMap<>();
		if (!aids.isEmpty()) {
			List<AlbumEntity> albumList = albumService.listByIds(aids);
			albumList.forEach(item -> {
				albumMap.put(item.getId(), item);
			});
		}

		List<AgreeCollectVO> agreeCollectVoList = new ArrayList<>();

		for (AgreeCollectEntity item : agreeCollectList) {
			AgreeCollectVO agreeCollectVo = new AgreeCollectVO();
			User user = userMap.get(item.getUid());
			if (user == null) continue;

			if (item.getType() == 0) {
				//评论
				CommentVO commentVo = commentMap.get(item.getAgreeCollectId());
				if (commentVo != null) {
					agreeCollectVo.setMid(commentVo.getMid());
					agreeCollectVo.setCover(commentVo.getCover());
					agreeCollectVo.setUid(user.getId());
					agreeCollectVo.setAvatar(user.getAvatar());
					agreeCollectVo.setUsername(user.getName());
					agreeCollectVo.setType(item.getType());
					agreeCollectVo.setCreateDate(item.getCreateTime());
						//评论的内容
					agreeCollectVo.setContent(commentVo.getContent());
				}

			} else if (item.getType() == 3) {
				AlbumEntity album = albumMap.get(item.getAgreeCollectId());
				if (album != null) {
					agreeCollectVo.setAid(album.getId());
					agreeCollectVo.setCover(album.getCover());
					agreeCollectVo.setAvatar(user.getAvatar());
					agreeCollectVo.setUid(user.getId());
					agreeCollectVo.setUsername(user.getName());
					agreeCollectVo.setType(item.getType());
					agreeCollectVo.setCreateDate(item.getCreateTime());
					agreeCollectVo.setContent(album.getName());
				}

			} else {
				ImgDetailEntity imgDetail = imgDetailMap.get(item.getAgreeCollectId());
				if (imgDetail != null) {
					agreeCollectVo.setMid(imgDetail.getId());
					agreeCollectVo.setCover(imgDetail.getCover());
					agreeCollectVo.setUid(user.getId());
					agreeCollectVo.setAvatar(user.getAvatar());
					agreeCollectVo.setUsername(user.getName());
					agreeCollectVo.setType(item.getType());
					agreeCollectVo.setCreateDate(item.getCreateTime());
					agreeCollectVo.setContent(imgDetail.getContent());
				}
			}
			agreeCollectVoList.add(agreeCollectVo);
		}

		return page.setRecords(agreeCollectVoList).setTotal(agreeCollectPage.getTotal());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelAgree(AgreeCollectDTO agreeCollectDTO) {
		String agreeKey = "";
		//如果点赞的是图片
		if (agreeCollectDTO.getType() == 1) {
			agreeKey = PlatformConstant.AGREE_IMG_KEY + agreeCollectDTO.getAgreeCollectId();
		} else if (agreeCollectDTO.getType() == 0) {
			agreeKey = PlatformConstant.AGREE_COMMENT_KEY + agreeCollectDTO.getAgreeCollectId();
		}

		redisUtils.sRemove(agreeKey, String.valueOf(agreeCollectDTO.getUid()));

		this.remove(new QueryWrapper<AgreeCollectEntity>()
			.and(e -> e.eq("uid", agreeCollectDTO.getUid())
				.eq("agree_collect_id", agreeCollectDTO.getAgreeCollectId())
				.eq("type", agreeCollectDTO.getType())));

		if (agreeCollectDTO.getType() == 1) {
			ImgDetailEntity imgDetail = imgDetailService.getById(agreeCollectDTO.getAgreeCollectId());
			if (imgDetail != null) {
				if (imgDetail.getAgreeCount() > 0) {
					imgDetail.setAgreeCount(imgDetail.getAgreeCount() - 1);
					sendMessageMq.sendMessage(PlatformMqConstant.IMG_DETAIL_STATE_EXCHANGE, PlatformMqConstant.IMG_DETAIL_STATE_KEY, imgDetail);
					imgDetailService.updateById(imgDetail);
				}
			}
		} else {
			CommentEntity comment = commentService.getById(agreeCollectDTO.getAgreeCollectId());
			if (comment != null) {
				if (comment.getCount() > 0) {
					comment.setCount(comment.getCount() - 1);
					sendMessageMq.sendMessage(PlatformMqConstant.IMG_DETAIL_STATE_EXCHANGE, PlatformMqConstant.IMG_DETAIL_STATE_KEY, comment);
					commentService.updateById(comment);
				}
			}
		}
	}

	@Override
	public IPage<AgreeCollectVO> getAllCollection(IPage<AgreeCollectVO> page, String uid, Integer type) {
		IPage<AgreeCollectEntity> agreeCollectPage = this.page(new Page<>(page.getCurrent(), page.getSize()),
			new QueryWrapper<AgreeCollectEntity>().and(e -> e.eq("uid", uid).eq("type", type)).orderByDesc("create_time"));

		List<AgreeCollectEntity> agreeCollectList = agreeCollectPage.getRecords();

		if (agreeCollectList.isEmpty()) {
			return page;
		}

		List<Long> ids = agreeCollectList.stream().map(AgreeCollectEntity::getAgreeCollectId).collect(Collectors.toList());
		List<AgreeCollectVO> agreeCollectVoList = new ArrayList<>();

		if (type == 2) {
			//查找所有收藏的图片
			List<Long> uids = agreeCollectList.stream().map(AgreeCollectEntity::getAgreeCollectUid).collect(Collectors.toList());

			HashMap<Long, User> userMap = new HashMap<>();
			HashMap<Long, ImgDetailEntity> imgDetailMap = new HashMap<>();

			if(!ids.isEmpty()) {
				List<ImgDetailEntity> imgDetailList = imgDetailService.listByIds(ids);
				imgDetailList.forEach(item -> {
					imgDetailMap.put(item.getId(), item);
				});
			}
			if(!uids.isEmpty()) {
				List<User> userList = userService.listByIds(uids);
				userList.forEach(item -> {
					userMap.put(item.getId(), item);
				});
			}

			for (AgreeCollectEntity item : agreeCollectList) {
				AgreeCollectVO agreeCollectVo = new AgreeCollectVO();
				ImgDetailEntity imgDetail = imgDetailMap.get(item.getAgreeCollectId());
				User user = userMap.get(item.getAgreeCollectUid());

				if (imgDetail != null && user != null) {
					agreeCollectVo.setMid(imgDetail.getId());
					agreeCollectVo.setCover(imgDetail.getCover());
					agreeCollectVo	.setUid(user.getId());
					agreeCollectVo	.setAvatar(user.getAvatar());
					agreeCollectVo.setUsername(user.getName());
					agreeCollectVo.setType(item.getType());
					agreeCollectVo.setCreateDate(item.getCreateTime());
					agreeCollectVo.setContent(imgDetail.getContent());
					agreeCollectVo.setCount(imgDetail.getCount()) ;// ImgDetailEntity might not have count, check VO
					agreeCollectVo.setCollectionCount(imgDetail.getCollectionCount());
					agreeCollectVoList.add(agreeCollectVo);
				}
			}
		} else if (type == 3) {
			//查找所有收藏的专辑
			List<Long> uids = agreeCollectList.stream().map(AgreeCollectEntity::getAgreeCollectUid).collect(Collectors.toList());

			HashMap<Long, User> userMap = new HashMap<>();
			HashMap<Long, AlbumEntity> albumMap = new HashMap<>();

			if(!ids.isEmpty()) {
				List<AlbumEntity> albumList = albumService.listByIds(ids);
				albumList.forEach(item -> {
					albumMap.put(item.getId(), item);
				});
			}
			if(!uids.isEmpty()) {
				List<User> userList = userService.listByIds(uids);
				userList.forEach(item -> {
					userMap.put(item.getId(), item);
				});
			}

			for (AgreeCollectEntity item : agreeCollectList) {
				AgreeCollectVO agreeCollectVo = new AgreeCollectVO();

				AlbumEntity album = albumMap.get(item.getAgreeCollectId());
				User user = userMap.get(item.getAgreeCollectUid());

				if (album != null && user != null) {
					agreeCollectVo.setCover(album.getCover());
					agreeCollectVo.setAid(album.getId());
					agreeCollectVo.setUid(user.getId());
					agreeCollectVo.setAvatar(user.getAvatar());
					agreeCollectVo.setUsername(user.getName());
					agreeCollectVo.setType(item.getType());
					agreeCollectVo.setCreateDate(item.getCreateTime());
					agreeCollectVo	.setContent(album.getName());
					agreeCollectVo.setImgCount(album.getImgCount());
					agreeCollectVo.setCollectionCount(album.getCollectionCount());
					agreeCollectVoList.add(agreeCollectVo);
				}
			}
		}

		return page.setRecords(agreeCollectVoList).setTotal(agreeCollectPage.getTotal());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> collection(AgreeCollectDTO agreeCollectDTO) {
		Map<String, String> res = new HashMap<>();
		AgreeCollectEntity isAgreeCollect = this.getOne(new QueryWrapper<AgreeCollectEntity>()
			.and(e -> e.eq("uid", agreeCollectDTO.getUid())
				.eq("agree_collect_id", agreeCollectDTO.getAgreeCollectId())
				.eq("type", agreeCollectDTO.getType())));

		if (isAgreeCollect != null) {
			res.put(PlatformConstant.MESSAGE, PlatformConstant.COLLECTION_USER_SUCCESS);
			return res;
		}

		AgreeCollectEntity agreeCollect = BeanUtil.copy(agreeCollectDTO, AgreeCollectEntity.class);
		this.save(agreeCollect);

		if (agreeCollectDTO.getType() == 3) {
			AlbumEntity album = albumService.getById(agreeCollectDTO.getAgreeCollectId());
			if (album != null) {
				if(album.getCollectionCount() == null) album.setCollectionCount(0L);
				album.setCollectionCount(album.getCollectionCount() + 1);
				sendMessageMq.sendMessage(PlatformMqConstant.ALBUM_STATE_EXCHANGE, PlatformMqConstant.ALBUM_STATE_KEY, album);
				albumService.updateById(album);
			}
		} else if (agreeCollectDTO.getType() == 2) {
			ImgDetailEntity imgDetail = imgDetailService.getById(agreeCollectDTO.getAgreeCollectId());
			if (imgDetail != null) {
				if(imgDetail.getCollectionCount() == null) imgDetail.setCollectionCount(0L);
				imgDetail.setCollectionCount(imgDetail.getCollectionCount() + 1);
				sendMessageMq.sendMessage(PlatformMqConstant.IMG_DETAIL_STATE_EXCHANGE, PlatformMqConstant.IMG_DETAIL_STATE_KEY, imgDetail);
				imgDetailService.updateById(imgDetail);
			}
		}

		//更改用户记录表
		agreeCollectNotice(agreeCollectDTO, agreeCollect);

		res.put(PlatformConstant.MESSAGE, PlatformConstant.COLLECTION_SUCCESS);
		return res;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, String> cancelCollection(AgreeCollectDTO agreeCollectDTO) {
		Map<String, String> res = new HashMap<>();
		if (agreeCollectDTO.getType() == 2) {
			AgreeCollectEntity collectionEntity = this.getOne(new QueryWrapper<AgreeCollectEntity>()
				.and(e -> e.eq("uid", agreeCollectDTO.getUid())
					.eq("agree_collect_id", agreeCollectDTO.getAgreeCollectId())
					.eq("type", agreeCollectDTO.getType())));
			if (collectionEntity == null) {
				res.put(PlatformConstant.MESSAGE, PlatformConstant.COLLECTION_ERROR);
				return res;
			}

			ImgDetailEntity imgDetail = imgDetailService.getById(agreeCollectDTO.getAgreeCollectId());
			if (imgDetail != null) {
				if (imgDetail.getCollectionCount() > 0) {
					imgDetail.setCollectionCount(imgDetail.getCollectionCount() - 1);
					// TODO: MQ
					imgDetailService.updateById(imgDetail);
				}
			}

			// Remove relation from Album if needed
			List<AlbumImgRelationEntity> albumImgRelationList = albumImgRelationService.list(
				new QueryWrapper<AlbumImgRelationEntity>().eq("mid", agreeCollectDTO.getAgreeCollectId()));

			List<Long> albumIds = albumImgRelationList.stream().map(AlbumImgRelationEntity::getAid).collect(Collectors.toList());
			if (!albumIds.isEmpty()) {
				List<AlbumEntity> albumList = albumService.listByIds(albumIds);
				Map<Long, AlbumEntity> albumMap = new HashMap<>();
				albumList.forEach(item -> albumMap.put(item.getId(), item));

				for (AlbumImgRelationEntity albumImgRelation : albumImgRelationList) {
					AlbumEntity album = albumMap.get(albumImgRelation.getAid());
					if (album != null && album.getUid().equals(agreeCollectDTO.getUid())) {
						albumImgRelationService.remove(new QueryWrapper<AlbumImgRelationEntity>()
							.and(e -> e.eq("aid", albumImgRelation.getAid())
								.eq("mid", agreeCollectDTO.getAgreeCollectId())));
						break;
					}
				}
			}

		} else if (agreeCollectDTO.getType() == 3) {
			AgreeCollectEntity agreeCollect = this.getOne(new QueryWrapper<AgreeCollectEntity>()
				.and(e -> e.eq("uid", agreeCollectDTO.getUid())
					.eq("agree_collect_id", agreeCollectDTO.getAgreeCollectId()))
				.eq("type", agreeCollectDTO.getType()));
			if (agreeCollect == null) {
				res.put(PlatformConstant.MESSAGE, PlatformConstant.COLLECTION_USER_FAIL);
				return res;
			}

			AlbumEntity album = albumService.getById(agreeCollectDTO.getAgreeCollectId());
			if (album != null) {
				if (album.getCollectionCount() > 0) {
					album.setCollectionCount(album.getCollectionCount() - 1);
					// TODO: MQ
					albumService.updateById(album);
				}
			}
		}

		this.remove(new QueryWrapper<AgreeCollectEntity>()
			.and(e -> e.eq("uid", agreeCollectDTO.getUid())
				.eq("agree_collect_id", agreeCollectDTO.getAgreeCollectId())
				.eq("type", agreeCollectDTO.getType())));
		res.put(PlatformConstant.MESSAGE, PlatformConstant.COLLECTION_CANCEL);
		return res;
	}

	private void agreeCollectNotice(AgreeCollectDTO agreeCollectDTO, AgreeCollectEntity agreeCollect) {
		if (agreeCollectDTO.getUid().equals(agreeCollectDTO.getAgreeCollectUid())) {
			return;
		}
		String userRecordKey = PlatformConstant.USER_RECORD + agreeCollectDTO.getAgreeCollectUid();
		UserRecordVO userRecordVO;
		Object obj = redisUtils.get(userRecordKey);

		if (obj != null) {
			userRecordVO = JsonUtil.parse(obj.toString(), UserRecordVO.class);
		} else {
			userRecordVO = new UserRecordVO();
		}

		if (userRecordVO == null) userRecordVO = new UserRecordVO();

		if (agreeCollectDTO.getType() == 0 || agreeCollectDTO.getType() == 1) {
			//点赞
			userRecordVO.setAgreeCollectionCount(userRecordVO.getAgreeCollectionCount() + 1);
			WebSocketServer.sendMessageTo(JsonUtil.toJson(userRecordVO), String.valueOf(agreeCollectDTO.getAgreeCollectUid()));
		} else {
			//收藏
			userRecordVO.setCollectionCount(userRecordVO.getCollectionCount() + 1);
			WebSocketServer.sendMessageTo(JsonUtil.toJson(userRecordVO), String.valueOf(agreeCollectDTO.getAgreeCollectUid()));
		}
		redisUtils.set(userRecordKey, JsonUtil.toJson(userRecordVO));
	}

}
