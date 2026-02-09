package org.springblade.modules.recommend.service;

import java.util.Map;

/**
 * 推荐服务类
 *
 * @author BladeX
 */
public interface IRecommendService {

    /**
     * 暂时随机推荐
     * @param page
     * @param limit
     * @param uid
     * @return
     */
    Map<String, Object> recommendToUserByCF(long page, long limit, String uid);

    /**
     * 使用机器学习模型做推荐系统
     * @param page
     * @param limit
     * @param uid
     * @return
     */
    Map<String, Object> recommendToUser(long page, long limit, String uid);
}
