package org.springblade.common.vo;

import lombok.Data;
import java.util.Date;
import java.io.Serializable;

@Data
public class TrendVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long mid;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private String imgsUrl;
    private Long albumId;
    private String albumName;
    private Long agreeCount;
    private Long commentCount;
    private Boolean isAgree;
    private Integer status;
    private Date time;
}
