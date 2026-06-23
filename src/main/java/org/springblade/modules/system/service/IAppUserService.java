package org.springblade.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.modules.system.pojo.entity.User;
import org.springblade.modules.system.pojo.vo.FollowVO;
import org.springblade.modules.system.pojo.vo.TrendVO;
import org.springblade.modules.system.pojo.vo.UserRecordVO;

import java.util.List;
import java.util.Map;

public interface IAppUserService {
    IPage<TrendVO> getTrendByUser(IPage<TrendVO> page, String userId, Integer type);
    IPage<FollowVO> searchUser(IPage<FollowVO> page, String keyword, String uid);
    UserRecordVO getUserRecord(String uid);
    void clearUserRecord(String uid, Integer type);
    User updateUser(User user);
    User getUserInfo(String uid);
    Map<String, Object> getUserAssets(String uid);
    Map<String, Object> getTalentHome(long page, long limit, String uid);
    List<FollowVO> searchUserByUsername(String keyword);
}
