package org.springblade.modules.imgDetail.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 浏览记录 DTO
 *
 * @author BladeX
 * @since 2026-01-28
 */
@Data
public class BrowseRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 图片ID
     */
    private String imgId;

    /**
     * 浏览时长(秒)
     */
    private Integer browseDuration;

    /**
     * 设备标识
     */
    private String deviceId;
}
