package org.springblade.common.cache.state;

import lombok.Data;

@Data
public class UserState {
    private Long uid;
    private Long trendCount;
    private Long followCount;
    private Long fanCount;
}