package org.springblade.modules.recommend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.RecommendConstant;
import org.springblade.common.constant.platform.PlatformConstant;

import org.springblade.common.utils.RedisUtils;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.imgDetail.pojo.vo.ImgDetailVO;
import org.springblade.modules.imgDetail.service.IImgDetailService;
import org.springblade.modules.recommend.service.IRecommendService;

import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类
 *
 * @author BladeX
 */
@Service
@Slf4j
public class RecommendServiceImpl implements IRecommendService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private IUserService userService;

    @Autowired
    private IImgDetailService imgDetailService;

    @Override
    public Map<String, Object> recommendToUserByCF(long page, long limit, String uid) {
        Map<String, Object> resMap = new HashMap<>(2);
        String ukey = RecommendConstant.BR_IMG_KEY + uid;

        List<ImgDetailEntity> imgDetailRecords = new ArrayList<>();
        if (redisUtils.hasKey(ukey)) {
            // 获取浏览记录
            List<String> mids = redisUtils.lRange(ukey, 0, 5);
            if (mids != null && !mids.isEmpty()) {
                // mids are JSON strings of ImgDetailVO in original code?
                // Wait, ImgDetailServiceImpl addBrowseRecord pushes JSON(vo).
                // But listByIds expects IDs.
                // Original code: List<ImgDetailEntity> imgDetailRecords = this.listByIds(mids);
                // This implies mids were IDs in original code OR listByIds handles it.
                // But redisUtils.lRange returns List<String>.
                // In ImgDetailServiceImpl.addBrowseRecord: redisUtils.lLeftPush(key, JSON.toJSONString(vo));
                // So lRange returns JSON strings of VOs.
                // We need to parse them to get IDs or check equality.

                for (String midJson : mids) {
                    try {
                        ImgDetailVO vo = JSON.parseObject(midJson, ImgDetailVO.class);
                        if (vo != null) {
                            ImgDetailEntity entity = BeanUtil.copy(vo, ImgDetailEntity.class);
                            imgDetailRecords.add(entity);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }

        String imgDetailKey = PlatformConstant.IMG_DETAIL_LIST_KEY;
        Map<Object, Object> imgMap = redisUtils.hGetAll(imgDetailKey);

        List<ImgDetailEntity> imgDetailList = new ArrayList<>();
        if (imgMap != null && !imgMap.isEmpty()) {
            for (Object val : imgMap.values()) {
                try {
                    ImgDetailEntity entity = JSON.parseObject((String) val, ImgDetailEntity.class);
                    imgDetailList.add(entity);
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        // Fallback to DB if Redis is empty
        if (imgDetailList.isEmpty()) {
            Page<ImgDetailEntity> pageParam = new Page<>(1, 100); // Fetch top 100 recent
            QueryWrapper<ImgDetailEntity> qw = new QueryWrapper<ImgDetailEntity>()
                    .eq("status", 1)
                    .orderByDesc("create_time");
            IPage<ImgDetailEntity> dbPage = imgDetailService.page(pageParam, qw);
            imgDetailList.addAll(dbPage.getRecords());
        }

        if (imgDetailList.isEmpty()) {
             resMap.put(RecommendConstant.RECORDS, new ArrayList<>());
             resMap.put(RecommendConstant.TOTAL, 0);
             return resMap;
        }

        // Shuffle for randomness
        Collections.shuffle(imgDetailList);

        // Pagination in memory (since we shuffled)
        // Note: Logic here is a bit weird for pagination, but following original intent of "Random Recommend"
        // Original logic partitioned the whole list.

        List<ImgDetailEntity> resultList = new ArrayList<>();
        int count = 0;
        for (ImgDetailEntity entity : imgDetailList) {
            if (count >= limit) break;
            // Filter out browsed
            boolean browsed = false;
            for (ImgDetailEntity record : imgDetailRecords) {
                if (record.getId().equals(entity.getId())) {
                    browsed = true;
                    break;
                }
            }
            if (!browsed) {
                resultList.add(entity);
                count++;
            }
        }

        List<ImgDetailVO> imgDetailVoList = populateUserInfo(resultList);

        resMap.put(RecommendConstant.RECORDS, imgDetailVoList);
        resMap.put(RecommendConstant.TOTAL, imgDetailList.size()); // Total available for recommend
        return resMap;
    }

    @Override
    public Map<String, Object> recommendToUser(long page, long limit, String uid) {
        // Placeholder for ML recommendation - defaulting to CF for now
        return recommendToUserByCF(page, limit, uid);
    }

    private List<ImgDetailVO> populateUserInfo(List<ImgDetailEntity> list) {
        List<ImgDetailVO> voList = BeanUtil.copy(list, ImgDetailVO.class);
        if (voList.isEmpty()) return voList;

        Set<Long> uids = voList.stream().map(ImgDetailVO::getUserId).collect(Collectors.toSet());
        if (uids.isEmpty()) return voList;

        List<User> users = userService.listByIds(uids);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        for (ImgDetailVO vo : voList) {
            User user = userMap.get(vo.getUserId());
            if (user != null) {
                vo.setUsername(user.getName());
                vo.setAvatar(user.getAvatar());
            }
        }
        return voList;
    }
}
