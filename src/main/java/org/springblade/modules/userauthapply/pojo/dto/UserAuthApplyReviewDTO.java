package org.springblade.modules.userauthapply.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户认证审核请求
 */
@Data
public class UserAuthApplyReviewDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "申请ID")
    private Long applyId;

    @Schema(description = "审核状态[1:通过,2:驳回]")
    private Integer auditStatus;

    @Schema(description = "审核意见")
    private String auditOpinion;
}
