package org.springblade.modules.pointsrulecondition.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.pointsrulecondition.pojo.entity.PointsRuleConditionEntity;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class PointsRuleConditionDTO extends PointsRuleConditionEntity {

    @Serial
    private static final long serialVersionUID = 1L;
}
