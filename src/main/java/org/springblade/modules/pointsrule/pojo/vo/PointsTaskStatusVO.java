package org.springblade.modules.pointsrule.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class PointsTaskStatusVO extends PointsRuleVO {

    @Serial
    private static final long serialVersionUID = 1L;

    private String taskStatus;
    private Integer completedToday;
    private Integer completedHistory;
    private Integer progressValue;
    private Integer progressTarget;
    private Integer continueDays;
    private Integer monthSigninDays;
    private Integer todayGrantCount;
    private Integer todayGrantPoints;
}
