package org.springblade.modules.pointsrulecondition.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.pointsrulecondition.excel.PointsRuleConditionExcel;
import org.springblade.modules.pointsrulecondition.mapper.PointsRuleConditionMapper;
import org.springblade.modules.pointsrulecondition.pojo.entity.PointsRuleConditionEntity;
import org.springblade.modules.pointsrulecondition.pojo.vo.PointsRuleConditionVO;
import org.springblade.modules.pointsrulecondition.service.IPointsRuleConditionService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PointsRuleConditionServiceImpl extends BaseServiceImpl<PointsRuleConditionMapper, PointsRuleConditionEntity> implements IPointsRuleConditionService {

    @Override
    public IPage<PointsRuleConditionVO> selectPointsRuleConditionPage(IPage<PointsRuleConditionVO> page, PointsRuleConditionVO pointsRuleCondition) {
        return baseMapper.selectPointsRuleConditionPage(page, pointsRuleCondition);
    }

    @Override
    public List<PointsRuleConditionExcel> exportPointsRuleCondition(Wrapper<PointsRuleConditionEntity> queryWrapper) {
        return baseMapper.exportPointsRuleCondition(queryWrapper);
    }

    @Override
    public List<PointsRuleConditionEntity> listByRuleCode(String ruleCode) {
        if (ruleCode == null || ruleCode.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return this.list(Wrappers.<PointsRuleConditionEntity>lambdaQuery()
            .eq(PointsRuleConditionEntity::getRuleCode, ruleCode)
            .eq(PointsRuleConditionEntity::getStatus, 1)
            .eq(PointsRuleConditionEntity::getIsDeleted, 0)
            .orderByAsc(PointsRuleConditionEntity::getConditionGroup)
            .orderByAsc(PointsRuleConditionEntity::getSort)
            .orderByAsc(PointsRuleConditionEntity::getId));
    }
}
