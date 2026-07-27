package org.springblade.modules.pointsrulecondition.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.pointsrulecondition.excel.PointsRuleConditionExcel;
import org.springblade.modules.pointsrulecondition.pojo.entity.PointsRuleConditionEntity;
import org.springblade.modules.pointsrulecondition.pojo.vo.PointsRuleConditionVO;

import java.util.List;

public interface IPointsRuleConditionService extends BaseService<PointsRuleConditionEntity> {

    IPage<PointsRuleConditionVO> selectPointsRuleConditionPage(IPage<PointsRuleConditionVO> page, PointsRuleConditionVO pointsRuleCondition);

    List<PointsRuleConditionExcel> exportPointsRuleCondition(Wrapper<PointsRuleConditionEntity> queryWrapper);

    List<PointsRuleConditionEntity> listByRuleCode(String ruleCode);
}
