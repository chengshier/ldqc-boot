package org.springblade.modules.system.pojo.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class FollowVO implements Serializable {
    private Long uid;
    private String username;
    private String avatar;
    private Integer fanCount;
    private String userId; // unique id string
    private Boolean isfollow;
}
