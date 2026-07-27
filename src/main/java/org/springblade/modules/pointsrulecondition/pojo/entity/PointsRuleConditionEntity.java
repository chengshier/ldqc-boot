package org.springblade.modules.pointsrulecondition.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

@Data
@TableName("points_rule_condition")
@Schema(description = "PointsRuleCondition对象")
@EqualsAndHashCode(callSuper = true)
public class PointsRuleConditionEntity extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String ruleCode;
    private Integer conditionGroup;
    private String conditionKey;
    private String conditionOp;
    private String conditionValue;
    private Integer sort;
    private Integer status;
    private String remark;
}
