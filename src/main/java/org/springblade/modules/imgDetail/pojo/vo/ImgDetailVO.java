package org.springblade.modules.imgDetail.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.imgDetail.pojo.entity.ImgDetailEntity;
import org.springblade.modules.tag.pojo.vo.TagVO;
import org.springblade.modules.album.pojo.entity.AlbumEntity;
import java.util.List;
import java.util.Date;

/**
 * 图片详情表 视图实体类
 *
 * @author BladeX
 * @since 2026-01-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImgDetailVO extends ImgDetailEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 标签列表
     */
    private List<TagVO> tagList;
    
    /**
     * 专辑
     */
    private AlbumEntity album;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 父分类ID
     */
    private Long categoryPid;
    
    /**
     * 父分类名称
     */
    private String categoryPName;
    
    /**
     * 发布时间
     */
    private Date time;
    
    /**
     * 其他用户ID
     */
    private Long otherUserId;
    /**
     * 当前用户是否已点赞
     */
    private Boolean isAgree;
}