package org.springblade.modules.imgDetail.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 社区图文与短视频内容实体。
 *
 * <p>status：0待审核、1已发布、2审核拒绝、3已下架。</p>
 */
@Data
@TableName("t_img_detail")
@EqualsAndHashCode(callSuper = true)
public class ImgDetailEntity extends TenantEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	/** 内容文案 */
	private String content;
	/** 图片封面 */
	private String cover;
	/** 发布用户ID，用户发布时由服务端登录态写入 */
	private Long userId;
	/** 二级运动分类 */
	private Long categoryId;
	/** 一级运动分类 */
	private Long categoryPid;
	/** 多图地址 JSON */
	private String imgsUrl;
	/** 媒体类型 IMAGE/VIDEO */
	private String mediaType;
	/** 媒体地址 */
	private String mediaUrl;
	/** 媒体封面或首帧 */
	private String posterUrl;
	/** 媒体时长，秒 */
	private Integer duration;
	/** 文件大小，字节 */
	private Long fileSize;
	/** 媒体宽度 */
	private Integer width;
	/** 媒体高度 */
	private Integer height;
	/** 图片数量 */
	private Integer count;
	/** 排序 */
	private Integer sort;
	/** 业务状态：0待审核、1已发布、2审核拒绝、3已下架 */
	private Integer status;
	/** 审核原因或下架原因 */
	private String auditReason;
	/** 审核时间 */
	private Date auditTime;
	/** 审核运营人员ID */
	private Long auditUserId;
	/** 正式发布时间 */
	private Date publishTime;
	/** 媒体处理状态：PROCESSING/READY/FAILED */
	private String mediaProcessStatus;
	/** 点赞数量 */
	private Long agreeCount;
	/** 收藏数量 */
	private Long collectionCount;
	/** 评论数量 */
	private Long commentCount;
	/** 浏览数量 */
	private Long viewCount;
}
