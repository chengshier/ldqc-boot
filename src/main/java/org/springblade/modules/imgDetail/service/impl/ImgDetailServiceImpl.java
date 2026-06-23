package org.springblade.modules.imgDetail.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springblade.common.utils.PlatformDataToCache;
import org.springblade.common.constant.PlatformMqConstant;
import org.springblade.common.constant.RecommendConstant;
import org.springblade.common.constant.platform.PlatformConstant;
import org.springblade.common.utils.RedisUtils;
import org.springblade.common.utils.SendMessageMq;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.album.service.IAlbumService;
import org.springblade.modules.albumimgrelation.pojo.entity.AlbumImgRelationEntity;
import org.springblade.modules.albumimgrelation.service.IAlbumImgRelationService;
import org.springblade.modules.category.pojo.entity.CategoryEntity;
import org.springblade.modules.category.service.ICategoryService;
import org.springblade.modules.imgDetail.mapper.ImgDetailMapper;
import org.springblade.modules.imgDetail.pojo.dto.BrowseRecordDTO;
import org.springblade.modules.imgDetail.pojo.dto.ImgDetailDTO;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.imgDetail.excel.ImgDetailExcel;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.talentpost.pojo.entity.TalentPostEntity;
import org.springblade.modules.talentpost.service.ITalentPostService;
import org.springblade.modules.tag.pojo.entity.TagEntity;
import org.springblade.modules.tag.pojo.vo.TagVO;
import org.springblade.modules.tag.service.ITagService;
import org.springblade.modules.tagimgrelation.pojo.entity.TagImgRelationEntity;
import org.springblade.modules.tagimgrelation.service.ITagImgRelationService;
import org.springblade.modules.userauthtype.pojo.entity.UserAuthTypeEntity;
import org.springblade.modules.userauthtype.service.IUserAuthTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * 图片详情表 服务实现类
 *
 * @author BladeX
 * @since 2026-01-28
 */
@Slf4j
@Service
public class ImgDetailServiceImpl extends BaseServiceImpl<ImgDetailMapper, ImgDetailEntity> implements IImgDetailService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private IUserService userService;

    @Autowired
    private ITagImgRelationService tagImgRelationService;

    @Autowired
    private ITagService tagService;

    @Autowired
    private SendMessageMq sendMessageMq;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IAlbumService albumService;

    @Autowired
    private IAlbumImgRelationService albumImgRelationService;

    @Autowired
    private PlatformDataToCache platformDataToCache;

    @Autowired
    private ITalentPostService talentPostService;

    @Autowired
    private IUserAuthTypeService userAuthTypeService;

    @Override
    public IPage<ImgDetailVO> getPage(IPage<ImgDetailVO> page) {
        IPage<ImgDetailEntity> entityPage = this.page(new Page<>(page.getCurrent(), page.getSize()));
        List<ImgDetailVO> voList = BeanUtil.copy(entityPage.getRecords(), ImgDetailVO.class);
        populateUserInfo(voList);
        page.setRecords(voList);
        page.setTotal(entityPage.getTotal());
        return page;
    }

    @Override
    public ImgDetailVO getImgDetail(String id) {
        ImgDetailEntity entity = this.getById(id);
        if (entity == null) return null;

        if (entity.getViewCount() <= 100) {
            entity.setViewCount(entity.getViewCount() + 1);
            this.updateById(entity);
            try {
                sendMessageMq.sendMessage(PlatformMqConstant.IMG_DETAIL_STATE_EXCHANGE, PlatformMqConstant.IMG_DETAIL_STATE_KEY, entity);
            } catch (Exception e) {
                log.error("Failed to send MQ message", e);
            }
        } else {
             String imgDetailStateKey = PlatformConstant.IMG_DETAIL_STATE + id;
             platformDataToCache.imgDetailDataToCache(entity, imgDetailStateKey, 3, 1);
        }

        ImgDetailVO vo = BeanUtil.copy(entity, ImgDetailVO.class);
        populateUserInfo(Collections.singletonList(vo));
        populateCategoryInfo(Collections.singletonList(vo));

        List<TagImgRelationEntity> relations = tagImgRelationService.list(new QueryWrapper<TagImgRelationEntity>().eq("mid", id));
        if (!relations.isEmpty()) {
            List<Long> tids = relations.stream().map(TagImgRelationEntity::getTid).collect(Collectors.toList());
            List<TagEntity> tags = tagService.listByIds(tids);
            vo.setTagList(BeanUtil.copy(tags, TagVO.class));
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(ImgDetailDTO imgDetailDTO) {
        ImgDetailEntity imgDetail = BeanUtil.copy(imgDetailDTO, ImgDetailEntity.class);
        imgDetail.setCollectionCount(0L);
        imgDetail.setCommentCount(0L);
        imgDetail.setAgreeCount(0L);
        imgDetail.setViewCount(0L);
        imgDetail.setStatus(1);
        this.save(imgDetail);

        AlbumImgRelationEntity albumImgRelation = new AlbumImgRelationEntity();
        albumImgRelation.setAid(imgDetailDTO.getAlbumId());
        albumImgRelation.setMid(imgDetail.getId());
        albumImgRelation.setSort(0);
        albumImgRelationService.save(albumImgRelation);

        org.springblade.modules.album.pojo.entity.AlbumEntity album = albumService.getById(imgDetailDTO.getAlbumId());
        if (album != null) {
            album.setImgCount((imgDetailDTO.getCount() == null ? 1 : imgDetailDTO.getCount()) + album.getImgCount());
            albumService.updateById(album);
        }

        List<TagImgRelationEntity> tagImgRelationList = new ArrayList<>();
        if (imgDetailDTO.getTags() != null) {
            for (TagEntity tag : imgDetailDTO.getTags()) {
                long id = tagService.saveTagByName(tag.getName());
                TagImgRelationEntity tagImgRelationEntity = new TagImgRelationEntity();
                tagImgRelationEntity.setMid(imgDetail.getId());
                tagImgRelationEntity.setTid(id);
                tagImgRelationList.add(tagImgRelationEntity);
            }
            tagImgRelationService.saveBatch(tagImgRelationList);
        }

        User user = userService.getById(imgDetail.getUserId());
        if (user != null) {
            sendMessageMq.sendMessage(PlatformMqConstant.USER_STATE_EXCHANGE, PlatformMqConstant.USER_STATE_KEY, user);
            syncTalentPostIfNeeded(imgDetail, imgDetailDTO, user);
        }

        redisUtils.hPut(PlatformConstant.IMG_DETAIL_LIST_KEY, String.valueOf(imgDetail.getId()), JSON.toJSONString(imgDetail));

        return imgDetail.getId();
    }

    @Override
    public List<ImgDetailVO> getAllBrowseRecordByUser(long page, long limit, String uid) {
        String key = RecommendConstant.BR_IMG_KEY + uid;
        if (!redisUtils.hasKey(key)) {
            return new ArrayList<>();
        }
        long start = (page - 1) * limit;
        long end = start + limit - 1;
        List<String> jsonList = redisUtils.lRange(key, start, end);
        if (jsonList == null || jsonList.isEmpty()) {
            return new ArrayList<>();
        }

        List<ImgDetailVO> voList = new ArrayList<>();
        for (String json : jsonList) {
            try {
                voList.add(JSON.parseObject(json, ImgDetailVO.class));
            } catch (Exception e) {
            }
        }
        return voList;
    }

    @Override
    public void addBrowseRecord(BrowseRecordDTO browseRecordDTO) {
        String key = RecommendConstant.BR_IMG_KEY + browseRecordDTO.getUserId();

        List<String> objs = redisUtils.lRange(key, 0, -1);
        if (objs != null) {
            for (String s : objs) {
                try {
                    ImgDetailVO vo = JSON.parseObject(s, ImgDetailVO.class);
                    if (vo.getId().toString().equals(browseRecordDTO.getImgId())) {
                        redisUtils.lRemove(key, 0, s);
                        break;
                    }
                } catch (Exception e) {
                }
            }
        }

        ImgDetailEntity entity = this.getById(browseRecordDTO.getImgId());
        if (entity != null) {
            ImgDetailVO vo = BeanUtil.copy(entity, ImgDetailVO.class);
            populateUserInfo(Collections.singletonList(vo));
            populateCategoryInfo(Collections.singletonList(vo));
            redisUtils.lLeftPush(key, JSON.toJSONString(vo));
        }
    }

    @Override
    public void delRecord(String uid, List<String> idList) {
        String key = RecommendConstant.BR_IMG_KEY + uid;
        List<String> objs = redisUtils.lRange(key, 0, -1);
        if (objs != null) {
            for (String id : idList) {
                for (String s : objs) {
                    try {
                        ImgDetailVO vo = JSON.parseObject(s, ImgDetailVO.class);
                        if (vo.getId().toString().equals(id)) {
                            redisUtils.lRemove(key, 0, s);
                            break;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Override
    public IPage<ImgDetailVO> getAllImgByAlbum(long page, long limit, String albumId, Integer type) {
        List<AlbumImgRelationEntity> relations = albumImgRelationService.list(new QueryWrapper<AlbumImgRelationEntity>().eq("aid", albumId));
        if (relations == null || relations.isEmpty()) {
            return new Page<>(page, limit);
        }
        List<Long> mids = relations.stream().map(AlbumImgRelationEntity::getMid).collect(Collectors.toList());
        QueryWrapper<ImgDetailEntity> qw = new QueryWrapper<ImgDetailEntity>().in("id", mids);
        if (type != null && type == 0) {
            qw.orderByDesc("create_time");
        } else {
            qw.orderByDesc("agree_count");
        }
        IPage<ImgDetailEntity> entityPage = this.page(new Page<>(page, limit), qw);
        List<ImgDetailVO> voList = BeanUtil.copy(entityPage.getRecords(), ImgDetailVO.class);
        populateUserInfo(voList);
        populateCategoryInfo(voList);

        IPage<ImgDetailVO> resultPage = new Page<>(page, limit);
        resultPage.setRecords(voList);
        resultPage.setTotal(entityPage.getTotal());
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImgs(List<Long> idList, Long uid) {
        if (idList == null || idList.isEmpty()) return;
        this.remove(new QueryWrapper<ImgDetailEntity>().in("id", idList).eq("user_id", uid));
        tagImgRelationService.remove(new QueryWrapper<TagImgRelationEntity>().in("mid", idList));
        albumImgRelationService.remove(new QueryWrapper<AlbumImgRelationEntity>().in("mid", idList));
    }

    @Override
    public IPage<ImgDetailVO> searchImgDetail(long page, long limit, String keyword, Integer type) {
        QueryWrapper<ImgDetailEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("content", keyword);
        if (type != null && type == 2) {
            queryWrapper.orderByDesc("create_time");
        } else {
            queryWrapper.orderByDesc("agree_count");
        }
        IPage<ImgDetailEntity> entityPage = this.page(new Page<>(page, limit), queryWrapper);
        List<ImgDetailVO> voList = BeanUtil.copy(entityPage.getRecords(), ImgDetailVO.class);
        populateUserInfo(voList);
        populateCategoryInfo(voList);
        IPage<ImgDetailVO> resultPage = new Page<>(page, limit);
        resultPage.setRecords(voList);
        resultPage.setTotal(entityPage.getTotal());
        return resultPage;
    }

    @Override
    public IPage<ImgDetailVO> selectImgDetailPage(IPage<ImgDetailVO> page, ImgDetailVO imgDetail) {
        return page.setRecords(baseMapper.selectImgDetailPage(page, imgDetail));
    }

    @Override
    public List<ImgDetailExcel> exportImgDetail(Wrapper<ImgDetailEntity> queryWrapper) {
        return baseMapper.exportImgDetail(queryWrapper);
    }

    @Override
    public void updateStatus(String id, Integer status) {
        ImgDetailEntity entity = new ImgDetailEntity();
        entity.setId(Func.toLong(id));
        entity.setStatus(status);
        this.updateById(entity);
    }

    @Override
    public void updateCommentCount(String id, int count) {
        ImgDetailEntity imgDetail = this.getById(id);
        if (imgDetail == null) return;

        if (imgDetail.getCommentCount() <= 100) {
            imgDetail.setCommentCount(imgDetail.getCommentCount() + count);
            this.updateById(imgDetail);
            sendMessageMq.sendMessage(PlatformMqConstant.IMG_DETAIL_STATE_EXCHANGE, PlatformMqConstant.IMG_DETAIL_STATE_KEY, imgDetail);
        } else {
            String imgDetailStateKey = PlatformConstant.IMG_DETAIL_STATE + id;
            platformDataToCache.imgDetailDataToCache(imgDetail, imgDetailStateKey, 2, count);
        }
    }

    private void populateUserInfo(List<ImgDetailVO> voList) {
        if (voList == null || voList.isEmpty()) return;
        Set<Long> uids = voList.stream().map(ImgDetailVO::getUserId).collect(Collectors.toSet());
        if (uids.isEmpty()) return;
        List<User> users = userService.listByIds(uids);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        for (ImgDetailVO vo : voList) {
            User user = userMap.get(vo.getUserId());
            if (user != null) {
                vo.setUsername(user.getName());
                vo.setAvatar(user.getAvatar());
            }
        }
    }

    private void populateCategoryInfo(List<ImgDetailVO> voList) {
        if (voList == null || voList.isEmpty()) return;
        Set<Long> cids = new HashSet<>();
        for (ImgDetailVO vo : voList) {
            if (vo.getCategoryId() != null) cids.add(vo.getCategoryId());
            if (vo.getCategoryPid() != null) cids.add(vo.getCategoryPid());
        }
        if (cids.isEmpty()) return;
        List<CategoryEntity> categories = categoryService.listByIds(cids);
        Map<Long, CategoryEntity> categoryMap = categories.stream().collect(Collectors.toMap(CategoryEntity::getId, c -> c));

        for (ImgDetailVO vo : voList) {
            if (vo.getCategoryId() != null && categoryMap.containsKey(vo.getCategoryId())) {
                vo.setCategoryName(categoryMap.get(vo.getCategoryId()).getName());
            }
            if (vo.getCategoryPid() != null && categoryMap.containsKey(vo.getCategoryPid())) {
                vo.setCategoryPName(categoryMap.get(vo.getCategoryPid()).getName());
            }
        }
    }

    private void syncTalentPostIfNeeded(ImgDetailEntity imgDetail, ImgDetailDTO imgDetailDTO, User user) {
        if (!isApprovedTalent(user)) {
            return;
        }

        TalentPostEntity talentPost = new TalentPostEntity();
        talentPost.setUserId(imgDetail.getUserId());
        talentPost.setTitle(resolveTalentPostTitle(imgDetail));
        talentPost.setContent(imgDetail.getContent());
        talentPost.setCoverImage(imgDetail.getCover());
        talentPost.setPosterUrl(resolvePrimaryPosterUrl(imgDetail.getPosterUrl(), imgDetail.getCover()));
        talentPost.setMediaUrl(resolvePrimaryMediaUrl(imgDetail));
        talentPost.setMediaType(resolveMediaType(imgDetail.getMediaType(), imgDetail.getImgsUrl()));
        talentPost.setDuration(imgDetail.getDuration());
        talentPost.setFileSize(imgDetail.getFileSize());
        talentPost.setWidth(imgDetail.getWidth());
        talentPost.setHeight(imgDetail.getHeight());
        talentPost.setAgreeCount(0);
        talentPost.setCommentCount(0);
        talentPost.setShareCount(0);
        talentPost.setViewCount(0);
        talentPost.setPostTag(resolveTalentPostTag(imgDetailDTO.getTags()));
        talentPostService.save(talentPost);
    }

    private boolean isApprovedTalent(User user) {
        if (user == null || user.getAuthStatus() == null || user.getAuthStatus() != 2) {
            return false;
        }

        UserAuthTypeEntity talentAuthType = findTalentAuthType();
        if (talentAuthType == null) {
            return matchesTalentIdentity(user.getMainIdentityCode(), user.getMainIdentityName(), "talent", "达人");
        }

        return matchesTalentIdentity(user.getMainIdentityCode(), user.getMainIdentityName(), talentAuthType.getCode(), talentAuthType.getName());
    }

    private UserAuthTypeEntity findTalentAuthType() {
        List<UserAuthTypeEntity> authTypes = userAuthTypeService.list();
        if (authTypes == null || authTypes.isEmpty()) {
            return null;
        }

        for (UserAuthTypeEntity authType : authTypes) {
            if (authType == null) {
                continue;
            }
            if (matchesTalentAuthType(authType)) {
                return authType;
            }
        }
        return null;
    }

    private boolean matchesTalentAuthType(UserAuthTypeEntity authType) {
        String code = Func.toStr(authType.getCode()).trim();
        String name = Func.toStr(authType.getName()).trim();
        String description = Func.toStr(authType.getDescription()).trim();
        return "talent".equalsIgnoreCase(code)
            || "达人".equals(name)
            || description.contains("达人");
    }

    private boolean matchesTalentIdentity(String userCode, String userName, String authTypeCode, String authTypeName) {
        String normalizedUserCode = Func.toStr(userCode).trim();
        String normalizedUserName = Func.toStr(userName).trim();
        String normalizedAuthTypeCode = Func.toStr(authTypeCode).trim();
        String normalizedAuthTypeName = Func.toStr(authTypeName).trim();
        return (!normalizedAuthTypeCode.isEmpty() && normalizedAuthTypeCode.equalsIgnoreCase(normalizedUserCode))
            || (!normalizedAuthTypeName.isEmpty() && normalizedAuthTypeName.equals(normalizedUserName));
    }

    private String resolvePrimaryMediaUrl(ImgDetailEntity imgDetail) {
        if (imgDetail == null) {
            return "";
        }
        if (imgDetail.getMediaUrl() != null && !imgDetail.getMediaUrl().trim().isEmpty()) {
            return imgDetail.getMediaUrl().trim();
        }
        String imgsUrl = imgDetail.getImgsUrl();
        if (imgsUrl != null && !imgsUrl.trim().isEmpty()) {
            try {
                Object parsed = JSON.parse(imgsUrl);
                if (parsed instanceof List<?> list && !list.isEmpty() && list.get(0) != null) {
                    return String.valueOf(list.get(0));
                }
            } catch (Exception ignored) {
            }
        }
        if (imgDetail.getCover() != null && !imgDetail.getCover().trim().isEmpty()) {
            return imgDetail.getCover().trim();
        }
        return "";
    }

    private String resolvePrimaryPosterUrl(String posterUrl, String cover) {
        if (posterUrl != null && !posterUrl.trim().isEmpty()) {
            return posterUrl.trim();
        }
        return cover;
    }

    private String resolveMediaType(String mediaType, String imgsUrl) {
        if (mediaType != null && !mediaType.trim().isEmpty()) {
            return mediaType.trim();
        }
        if (imgsUrl != null && !imgsUrl.trim().isEmpty()) {
            return "image";
        }
        return "image";
    }

    private String resolveTalentPostTag(List<TagEntity> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        for (TagEntity tag : tags) {
            if (tag != null && tag.getName() != null && !tag.getName().trim().isEmpty()) {
                return tag.getName().trim();
            }
        }
        return "";
    }

    private String resolveTalentPostTitle(ImgDetailEntity imgDetail) {
        String content = Func.toStr(imgDetail == null ? null : imgDetail.getContent()).trim();
        if (content.isEmpty()) {
            return "达人动态";
        }
        return content.length() > 30 ? content.substring(0, 30) : content;
    }
}
