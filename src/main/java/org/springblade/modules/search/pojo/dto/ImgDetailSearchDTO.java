package org.springblade.modules.search.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 图片详情搜索DTO
 */
@Data
public class ImgDetailSearchDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 排序类型：0是指全部 1是指点赞排序 2是指时间排序
     */
    private Integer type;

    /**
     * 当前页
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer limit = 10;
}