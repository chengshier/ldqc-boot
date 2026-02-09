package org.springblade.modules.imgDetail.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;
import java.io.Serial;

/**
 * 图片详情表 实体类
 *
 * @author BladeX
 * @since 2026-01-28
 */
@Data
@TableName("t_img_detail")
@EqualsAndHashCode(callSuper = true)
public class ImgDetailEntity extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 图片信息内容
     */
    private String content;

    /**
     * 图片封面
     */
    private String cover;

    /**
     * 发布图片信息的用户id
     */
    private Long userId;

    /**
     * 图片所属的二级分类
     */
    private Long categoryId;

    /**
     * 图片所属的一级分类
     */
    private Long categoryPid;

    /**
     * 图片的地址信息
     */
    private String imgsUrl;

    /**
     * 图片数量
     */
    private Integer count;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图片的状态
     */
    private Integer status;

    /**
     * 点赞数量
     */
    private Long agreeCount;

    /**
     * 收藏数量
     */
    private Long collectionCount;

    /**
     * 评论数量
     */
    private Long commentCount;

    /**
     * 浏览数量
     */
    private Long viewCount;
}
