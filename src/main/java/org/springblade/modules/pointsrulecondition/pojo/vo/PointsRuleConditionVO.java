package org.springblade.modules.pointsrulecondition.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.pointsrulecondition.pojo.entity.PointsRuleConditionEntity;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class PointsRuleConditionVO extends PointsRuleConditionEntity {

    @Serial
    private static final long serialVersionUID = 1L;
}
