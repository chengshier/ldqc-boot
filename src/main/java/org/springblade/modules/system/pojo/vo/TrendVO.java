package org.springblade.modules.system.pojo.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class TrendVO implements Serializable {
    private Long mid; // ImgDetail ID
    private Integer status;
    private Date time;
    private Long albumId;
    private String albumName;
    private String cover; // ImgDetail cover
    private String content; // ImgDetail content
}
