package org.springblade.modules.search.pojo.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 搜索记录VO
 */
@Data
public class SearchRecordVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String keyword;

    private String highLightKeyword;

    private Integer count;

    // 创建时间
    private String time;
}