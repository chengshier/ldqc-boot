package org.springblade.modules.pointsrulecondition.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.pointsrulecondition.pojo.entity.PointsRuleConditionEntity;
import org.springblade.modules.pointsrulecondition.pojo.vo.PointsRuleConditionVO;

import java.util.Objects;

public class PointsRuleConditionWrapper extends BaseEntityWrapper<PointsRuleConditionEntity, PointsRuleConditionVO> {

    public static PointsRuleConditionWrapper build() {
        return new PointsRuleConditionWrapper();
    }

    @Override
    public PointsRuleConditionVO entityVO(PointsRuleConditionEntity entity) {
        return Objects.requireNonNull(BeanUtil.copyProperties(entity, PointsRuleConditionVO.class));
    }
}
