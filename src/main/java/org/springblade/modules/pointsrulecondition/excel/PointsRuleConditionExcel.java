package org.springblade.modules.pointsrulecondition.excel;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class PointsRuleConditionExcel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ColumnWidth(20)
    @ExcelProperty("主键")
    private Long id;

    @ColumnWidth(20)
    @ExcelProperty("规则编码")
    private String ruleCode;

    @ColumnWidth(20)
    @ExcelProperty("条件组")
    private Integer conditionGroup;

    @ColumnWidth(20)
    @ExcelProperty("条件字段")
    private String conditionKey;

    @ColumnWidth(20)
    @ExcelProperty("条件运算符")
    private String conditionOp;

    @ColumnWidth(28)
    @ExcelProperty("条件值")
    private String conditionValue;

    @ColumnWidth(20)
    @ExcelProperty("排序")
    private Integer sort;

    @ColumnWidth(20)
    @ExcelProperty("状态")
    private Integer status;

    @ColumnWidth(28)
    @ExcelProperty("备注")
    private String remark;

    @ColumnWidth(20)
    @ExcelProperty("是否已删除")
    private Integer isDeleted;

    @ColumnWidth(20)
    @ExcelProperty("租户ID")
    private String tenantId;
}
