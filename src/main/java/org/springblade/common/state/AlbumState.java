package org.springblade.common.state;

import lombok.Data;
import java.io.Serializable;

@Data
public class AlbumState implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long aid;
    private Long collectionCount = 0L;
}
