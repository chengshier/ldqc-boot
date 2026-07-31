package org.springblade.modules.recommendfeedback.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 推荐曝光、点击、停留、播放完成和负反馈请求。 */
@Data
@Schema(description = "推荐行为反馈请求")
public class RecommendFeedbackRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Size(max = 64, message = "推荐反馈请求号长度不能超过64")
	@Schema(description = "客户端幂等请求号；为空时由服务端生成")
	private String requestId;

	@Size(max = 64, message = "推荐会话号长度不能超过64")
	@Schema(description = "推荐会话号")
	private String sessionId;

	@NotBlank(message = "推荐内容类型不能为空")
	@Size(max = 16, message = "推荐内容类型长度不能超过16")
	@Schema(description = "内容类型 CONTENT/NEWS", requiredMode = Schema.RequiredMode.REQUIRED)
	private String contentType;

	@NotNull(message = "推荐内容ID不能为空")
	@Positive(message = "推荐内容ID必须大于0")
	@Schema(description = "内容ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private Long contentId;

	@NotBlank(message = "推荐反馈类型不能为空")
	@Size(max = 24, message = "推荐反馈类型长度不能超过24")
	@Schema(description = "事件类型 IMPRESSION/CLICK/DWELL/VIDEO_COMPLETE/NOT_INTERESTED", requiredMode = Schema.RequiredMode.REQUIRED)
	private String eventType;

	@PositiveOrZero(message = "停留或播放时长不能为负数")
	@Schema(description = "停留或播放时长，毫秒")
	private Long durationMs;

	@Size(max = 2000, message = "推荐反馈扩展信息长度不能超过2000")
	@Schema(description = "扩展JSON")
	private String extraJson;
}
