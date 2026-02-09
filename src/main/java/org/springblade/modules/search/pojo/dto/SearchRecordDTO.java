package org.springblade.modules.search.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 搜索记录DTO
 */
@Data
public class SearchRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 关键词
     */
    private String keyword;
}