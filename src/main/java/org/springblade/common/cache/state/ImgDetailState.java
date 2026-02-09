package org.springblade.common.cache.state;

import lombok.Data;

@Data
public class ImgDetailState {
    private Long mid;
    private Long agreeCount;
    private Long collectionCount;
    private Long commentCount;
    private Long viewCount;
}