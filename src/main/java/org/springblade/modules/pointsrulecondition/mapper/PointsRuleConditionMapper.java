package org.springblade.modules.pointsrulecondition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.springblade.modules.pointsrulecondition.excel.PointsRuleConditionExcel;
import org.springblade.modules.pointsrulecondition.pojo.entity.PointsRuleConditionEntity;
import org.springblade.modules.pointsrulecondition.pojo.vo.PointsRuleConditionVO;

import java.util.List;

public interface PointsRuleConditionMapper extends BaseMapper<PointsRuleConditionEntity> {

    IPage<PointsRuleConditionVO> selectPointsRuleConditionPage(IPage<PointsRuleConditionVO> page, PointsRuleConditionVO pointsRuleCondition);

    List<PointsRuleConditionExcel> exportPointsRuleCondition(@Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<PointsRuleConditionEntity> queryWrapper);
}
