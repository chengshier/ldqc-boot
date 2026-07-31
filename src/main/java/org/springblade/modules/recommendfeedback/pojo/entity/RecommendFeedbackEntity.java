package org.springblade.modules.recommendfeedback.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.util.Date;

/** 推荐曝光、点击、停留、播放完成和负反馈事件。 */
@Data
@TableName("ldqc_recommend_feedback")
@Schema(description = "推荐反馈事件")
@EqualsAndHashCode(callSuper = true)
public class RecommendFeedbackEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String requestId;
	private Long userId;
	private String sessionId;
	/** CONTENT/NEWS */
	private String contentType;
	private Long contentId;
	/** IMPRESSION/CLICK/DWELL/VIDEO_COMPLETE/NOT_INTERESTED */
	private String eventType;
	private Long durationMs;
	private String extraJson;
	private Date occurredAt;
}
