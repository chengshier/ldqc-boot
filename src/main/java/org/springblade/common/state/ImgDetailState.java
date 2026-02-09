package org.springblade.common.state;

import lombok.Data;
import java.io.Serializable;

@Data
public class ImgDetailState implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long mid;
    private Long agreeCount = 0L;
    private Long commentCount = 0L;
    private Long collectionCount = 0L;
    private Long viewCount = 0L;
}
