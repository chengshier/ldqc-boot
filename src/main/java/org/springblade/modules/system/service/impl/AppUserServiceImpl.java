package org.springblade.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.PlatformConstant;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.album.pojo.entity.AlbumEntity;
import org.springblade.modules.album.service.IAlbumService;
import org.springblade.modules.albumimgrelation.pojo.entity.AlbumImgRelationEntity;
import org.springblade.modules.albumimgrelation.service.IAlbumImgRelationService;
import org.springblade.modules.follow.pojo.entity.FollowEntity;
import org.springblade.modules.follow.service.IFollowService;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.pointsaccount.pojo.entity.PointsAccountEntity;
import org.springblade.modules.pointsaccount.service.IPointsAccountService;
import org.springblade.modules.usercoupon.pojo.entity.UserCouponEntity;
import org.springblade.modules.usercoupon.service.IUserCouponService;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.pojo.vo.FollowVO;
import org.springblade.modules.system.pojo.vo.TrendVO;
import org.springblade.modules.system.pojo.vo.UserRecordVO;
import org.springblade.modules.system.service.IAppUserService;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.talentpost.pojo.entity.TalentPostEntity;
import org.springblade.modules.talentpost.service.ITalentPostService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AppUserServiceImpl implements IAppUserService {

    private final IUserService userService;
    private final IImgDetailService imgDetailService;
    private final IAlbumService albumService;
    private final IAlbumImgRelationService albumImgRelationService;
    private final IFollowService followService;
    private final ITalentPostService talentPostService;
    private final IPointsAccountService pointsAccountService;
    private final IUserCouponService userCouponService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public IPage<TrendVO> getTrendByUser(IPage<TrendVO> page, String userId, Integer type) {
        QueryWrapper<ImgDetailEntity> qw = new QueryWrapper<ImgDetailEntity>().eq("user_id", userId).orderByDesc("update_time");
        if (type != null && type == 0) {
            qw.eq("status", 1);
        }

        IPage<ImgDetailEntity> imgPage = imgDetailService.page(new Page<>(page.getCurrent(), page.getSize()), qw);

        List<ImgDetailEntity> imgList = imgPage.getRecords();
        List<TrendVO> trendList = new ArrayList<>();

        if (imgList != null && !imgList.isEmpty()) {
            List<Long> mids = imgList.stream().map(ImgDetailEntity::getId).collect(Collectors.toList());
            List<AlbumImgRelationEntity> relations = albumImgRelationService.list(new QueryWrapper<AlbumImgRelationEntity>().in("mid", mids));

            Map<Long, Long> midToAidMap = new HashMap<>();
            List<Long> aids = new ArrayList<>();
            for (AlbumImgRelationEntity rel : relations) {
                midToAidMap.put(rel.getMid(), rel.getAid());
                aids.add(rel.getAid());
            }

            Map<Long, AlbumEntity> albumMap = new HashMap<>();
            if (!aids.isEmpty()) {
                List<AlbumEntity> albums = albumService.listByIds(aids);
                for (AlbumEntity album : albums) {
                    albumMap.put(album.getId(), album);
                }
            }

            for (ImgDetailEntity img : imgList) {
                TrendVO vo = BeanUtil.copy(img, TrendVO.class);
                vo.setMid(img.getId());

                Long aid = midToAidMap.get(img.getId());
                if (aid != null) {
                    AlbumEntity album = albumMap.get(aid);
                    if (album != null) {
                        vo.setAlbumId(album.getId());
                        vo.setAlbumName(album.getName());
                    }
                }
                trendList.add(vo);
            }
        }
        return page.setRecords(trendList).setTotal(imgPage.getTotal());
    }

    @Override
    public IPage<FollowVO> searchUser(IPage<FollowVO> page, String keyword, String uid) {
        IPage<User> userPage = userService.page(new Page<>(page.getCurrent(), page.getSize()),
            new QueryWrapper<User>().like("account", keyword).or().like("name", keyword));

        List<Long> followIds = new ArrayList<>();
        if (followService != null) {
            List<FollowEntity> follows = followService.list(new QueryWrapper<FollowEntity>().eq("uid", uid));
            followIds = follows.stream().map(FollowEntity::getFid).collect(Collectors.toList());
        }

        List<FollowVO> voList = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            FollowVO vo = new FollowVO();
            vo.setUid(user.getId());
            vo.setUsername(user.getName());
            vo.setAvatar(user.getAvatar());
            vo.setIsfollow(followIds.contains(user.getId()));
            voList.add(vo);
        }
        return page.setRecords(voList).setTotal(userPage.getTotal());
    }

    @Override
    public UserRecordVO getUserRecord(String uid) {
        String key = PlatformConstant.USER_RECORD + uid;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return JsonUtil.parse(redisTemplate.opsForValue().get(key), UserRecordVO.class);
        }
        return new UserRecordVO();
    }

    @Override
    public void clearUserRecord(String uid, Integer type) {
        String key = PlatformConstant.USER_RECORD + uid;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            UserRecordVO vo = JsonUtil.parse(redisTemplate.opsForValue().get(key), UserRecordVO.class);
            if (vo != null) {
                if (type == 1) {
                    vo.setAgreeCollectionCount(0L);
                } else if (type == 2) {
                    vo.setAddFollowCount(0L);
                } else {
                    vo.setNoreplyCount(0L);
                }
                redisTemplate.opsForValue().set(key, JsonUtil.toJson(vo));
            }
        }
    }

    @Override
    public User updateUser(User user) {
        userService.updateById(user);
        return userService.getById(user.getId());
    }

    @Override
    public User getUserInfo(String uid) {
        User user = userService.getById(uid);
        if (user == null) {
            return null;
        }

        long followCount = followService.count(new QueryWrapper<FollowEntity>().eq("uid", uid));
        long fanCount = followService.count(new QueryWrapper<FollowEntity>().eq("fid", uid));
        UserRecordVO recordVO = getUserRecord(uid);
        long likeCount = recordVO == null || recordVO.getAgreeCollectionCount() == null ? 0L : recordVO.getAgreeCollectionCount();
        long collectCount = recordVO == null || recordVO.getCollectionCount() == null ? 0L : recordVO.getCollectionCount();
        long trendCount = imgDetailService.count(new QueryWrapper<ImgDetailEntity>().eq("user_id", uid));

        user.setFollowCount(followCount);
        user.setFanCount(fanCount);
        user.setLikeCount(likeCount);
        user.setCollectCount(collectCount);
        user.setTrendCount(trendCount);
        return user;
    }

    @Override
    public Map<String, Object> getUserAssets(String uid) {
        Map<String, Object> result = new HashMap<>();
        if (uid == null || uid.trim().isEmpty()) {
            return result;
        }

        PointsAccountEntity account = pointsAccountService.getOne(
            new QueryWrapper<PointsAccountEntity>().eq("user_id", uid).eq("is_deleted", 0)
        );
        long availablePoints = account == null || account.getAvailablePoints() == null ? 0L : account.getAvailablePoints();
        long couponCount = userCouponService.count(
            new QueryWrapper<UserCouponEntity>().eq("user_id", uid).eq("is_deleted", 0)
        );
        long usableCouponCount = userCouponService.count(
            new QueryWrapper<UserCouponEntity>()
                .eq("user_id", uid)
                .eq("is_deleted", 0)
                .eq("status", "UNUSED")
        );

        result.put("uid", uid);
        result.put("availablePoints", availablePoints);
        result.put("couponCount", couponCount);
        result.put("usableCouponCount", usableCouponCount);
        return result;
    }

    @Override
    public Map<String, Object> getTalentHome(long page, long limit, String uid) {
        Map<String, Object> result = new HashMap<>();
        if (uid == null || uid.trim().isEmpty()) {
            return result;
        }
        User user = getUserInfo(uid);
        if (user == null) {
            return result;
        }
        IPage<TalentPostEntity> postPage = talentPostService.page(
            new Page<>(page, limit),
            new QueryWrapper<TalentPostEntity>().eq("user_id", uid).orderByDesc("create_time")
        );
        result.put("user", user);
        result.put("posts", postPage.getRecords());
        result.put("total", postPage.getTotal());
        result.put("current", postPage.getCurrent());
        result.put("size", postPage.getSize());
        return result;
    }

    @Override
    public List<FollowVO> searchUserByUsername(String keyword) {
        List<User> list = userService.list(new QueryWrapper<User>().eq("name", keyword));
        List<FollowVO> voList = new ArrayList<>();
        for (User user : list) {
            FollowVO vo = new FollowVO();
            vo.setUid(user.getId());
            vo.setUsername(user.getName());
            vo.setAvatar(user.getAvatar());
            voList.add(vo);
        }
        return voList;
    }
}
