package org.springblade.common.state;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserState implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long uid;
    private Integer trendCount = 0;
    private Integer followCount = 0;
    private Integer fanCount = 0;
}
