package org.springblade.common.state;

import lombok.Data;
import java.io.Serializable;

@Data
public class CommentState implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long cid;
    private Integer count = 0;
    private Integer twoNums = 0;
}
