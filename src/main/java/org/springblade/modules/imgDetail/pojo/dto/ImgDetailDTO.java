package org.springblade.modules.imgDetail.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.tag.pojo.entity.TagEntity;
import org.springblade.modules.album.pojo.entity.AlbumEntity;
import java.util.List;

/**
 * 图片详情表 数据传输对象实体类
 *
 * @author BladeX
 * @since 2026-01-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImgDetailDTO extends ImgDetailEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 专辑ID
     */
    private Long albumId;

    /**
     * 标签列表
     */
    private List<TagEntity> tags;
    
    /**
     * 专辑对象
     */
    private AlbumEntity album;
}