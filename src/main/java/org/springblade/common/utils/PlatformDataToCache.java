package org.springblade.common.utils;

import com.alibaba.fastjson.JSON;
import org.springblade.modules.album.pojo.entity.AlbumEntity;
import org.springblade.modules.comment.pojo.entity.CommentEntity;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.common.cache.state.AlbumState;
import org.springblade.common.cache.state.CommentState;
import org.springblade.common.cache.state.ImgDetailState;
import org.springblade.common.cache.state.UserState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author BladeX
 */
@Component
public class PlatformDataToCache {

    @Autowired
    RedisUtils redisUtils;

    public void albumDataToCache(AlbumEntity album, String key, int value) {
        AlbumState albumState;
        if (Boolean.TRUE.equals(redisUtils.hasKey(key))) {
            albumState = JSON.parseObject(redisUtils.get(key), AlbumState.class);
            albumState.setCollectionCount(albumState.getCollectionCount() + value);
        } else {
            albumState = new AlbumState();
            albumState.setAid(album.getId());
            albumState.setCollectionCount(album.getCollectionCount() == null ? value : album.getCollectionCount() + value);
        }
        redisUtils.set(key, JSON.toJSONString(albumState));
    }

    public void userDataToCache(User user, String key, int type, int value) {
        UserState userState;
        if (Boolean.TRUE.equals(redisUtils.hasKey(key))) {
            userState = JSON.parseObject(redisUtils.get(key), UserState.class);
            if (type == 0) {
                userState.setTrendCount(userState.getTrendCount() + value);
            } else if (type == 1) {
                userState.setFollowCount(userState.getFollowCount() + value);
            } else {
                userState.setFanCount(userState.getFanCount() + value);
            }
        } else {
            userState = new UserState();
            userState.setUid(user.getId());
            userState.setTrendCount(user.getTrendCount() == null ? 0 : user.getTrendCount());
            userState.setFollowCount(user.getFollowCount() == null ? 0 : user.getFollowCount());
            userState.setFanCount(user.getFanCount() == null ? 0 : user.getFanCount());

            if (type == 0) {
                userState.setTrendCount(userState.getTrendCount() + value);
            } else if (type == 1) {
                userState.setFollowCount(userState.getFollowCount() + value);
            } else {
                userState.setFanCount(userState.getFanCount() + value);
            }
        }
        redisUtils.set(key, JSON.toJSONString(userState));
    }

    public void commentDataToCache(CommentEntity comment, String key, int type, int value) {
        CommentState commentState;
        if (Boolean.TRUE.equals(redisUtils.hasKey(key))) {
            commentState = JSON.parseObject(redisUtils.get(key), CommentState.class);
            if (type == 0) {
                commentState.setCount(commentState.getCount() + value);
            } else if (type == 1) {
                commentState.setTwoNums(commentState.getTwoNums() + value);
            }
        } else {
            commentState = new CommentState();
            commentState.setCid(comment.getId());
            commentState.setCount(comment.getCount() == null ? 0 : comment.getCount());
            commentState.setTwoNums(comment.getTwoNums() == null ? 0 : comment.getTwoNums());

            if (type == 0) {
                commentState.setCount(commentState.getCount() + value);
            } else if (type == 1) {
                commentState.setTwoNums(commentState.getTwoNums() + value);
            }
        }
        redisUtils.set(key, JSON.toJSONString(commentState));
    }

    public void imgDetailDataToCache(ImgDetailEntity imgDetail, String key, int type, int value) {
        ImgDetailState imgDetailState;
        if (Boolean.TRUE.equals(redisUtils.hasKey(key))) {
            imgDetailState = JSON.parseObject(redisUtils.get(key), ImgDetailState.class);
            if (type == 0) {
                imgDetailState.setAgreeCount(imgDetailState.getAgreeCount() + value);
            } else if (type == 1) {
                imgDetailState.setCollectionCount(imgDetailState.getCollectionCount() + value);
            } else if (type == 2) {
                imgDetailState.setCommentCount(imgDetailState.getCommentCount() + value);
            } else {
                imgDetailState.setViewCount(imgDetailState.getViewCount() + value);
            }
        } else {
            imgDetailState = new ImgDetailState();
            imgDetailState.setMid(imgDetail.getId());
            imgDetailState.setAgreeCount(imgDetail.getAgreeCount() == null ? 0L : imgDetail.getAgreeCount());
            imgDetailState.setCommentCount(imgDetail.getCommentCount() == null ? 0L : imgDetail.getCommentCount());
            imgDetailState.setCollectionCount(imgDetail.getCollectionCount() == null ? 0L : imgDetail.getCollectionCount());
            imgDetailState.setViewCount(imgDetail.getViewCount() == null ? 0L : imgDetail.getViewCount());

            if (type == 0) {
                imgDetailState.setAgreeCount(imgDetailState.getAgreeCount() + value);
            } else if (type == 1) {
                imgDetailState.setCollectionCount(imgDetailState.getCollectionCount() + value);
            } else if (type == 2) {
                imgDetailState.setCommentCount(imgDetailState.getCommentCount() + value);
            } else {
                imgDetailState.setViewCount(imgDetailState.getViewCount() + value);
            }
        }
        redisUtils.set(key, JSON.toJSONString(imgDetailState));
    }
}
