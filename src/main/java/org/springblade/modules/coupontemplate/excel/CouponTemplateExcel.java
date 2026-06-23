/**
 * 优惠券模板 Excel实体类
 */
package org.springblade.modules.coupontemplate.excel;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class CouponTemplateExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;

	@ColumnWidth(20)
	@ExcelProperty("券编码")
	private String couponCode;

	@ColumnWidth(20)
	@ExcelProperty("券名称")
	private String couponName;

	@ColumnWidth(20)
	@ExcelProperty("券类型")
	private String couponType;

	@ColumnWidth(20)
	@ExcelProperty("权益模式")
	private String benefitMode;

	@ColumnWidth(20)
	@ExcelProperty("满减门槛(分)")
	private Integer thresholdAmount;

	@ColumnWidth(20)
	@ExcelProperty("减免金额(分)/折扣值")
	private Integer discountAmount;

	@ColumnWidth(20)
	@ExcelProperty("最大减免金额(分)")
	private Integer maxDiscountAmount;

	@ColumnWidth(20)
	@ExcelProperty("时长券总分钟")
	private Integer durationMinutes;

	@ColumnWidth(20)
	@ExcelProperty("次数券总次数")
	private Integer totalTimes;

	@ColumnWidth(20)
	@ExcelProperty("抵扣目标类型")
	private String deductTargetType;

	@ColumnWidth(20)
	@ExcelProperty("抵扣目标ID")
	private String deductTargetId;

	@ColumnWidth(20)
	@ExcelProperty("单位抵扣金额(分)")
	private Integer deductUnitAmount;

	@ColumnWidth(20)
	@ExcelProperty("适用范围")
	private String scopeType;

	@ColumnWidth(20)
	@ExcelProperty("范围引用ID")
	private String scopeRefId;

	@ColumnWidth(20)
	@ExcelProperty("总库存")
	private Integer totalStock;

	@ColumnWidth(20)
	@ExcelProperty("剩余库存")
	private Integer remainStock;

	@ColumnWidth(20)
	@ExcelProperty("每用户限领数")
	private Integer perUserLimit;

	@ColumnWidth(20)
	@ExcelProperty("最低成长等级")
	private Integer minGrowthLevel;

	@ColumnWidth(20)
	@ExcelProperty("有效期类型")
	private String validType;

	@ColumnWidth(20)
	@ExcelProperty("固定生效开始时间")
	private Date validStartAt;

	@ColumnWidth(20)
	@ExcelProperty("固定生效结束时间")
	private Date validEndAt;

	@ColumnWidth(20)
	@ExcelProperty("领取后有效天数")
	private Integer validDays;

	@ColumnWidth(20)
	@ExcelProperty("获取方式")
	private String acquireType;

	@ColumnWidth(20)
	@ExcelProperty("兑换所需绿豆")
	private Integer costPoints;

	@ColumnWidth(20)
	@ExcelProperty("状态")
	private Integer status;

	@ColumnWidth(20)
	@ExcelProperty("扩展配置JSON")
	private String extJson;

	@ColumnWidth(20)
	@ExcelProperty("创建人")
	private Long createUser;

	@ColumnWidth(20)
	@ExcelProperty("创建部门")
	private Long createDept;

	@ColumnWidth(20)
	@ExcelProperty("创建时间")
	private Date createTime;

	@ColumnWidth(20)
	@ExcelProperty("修改人")
	private Long updateUser;

	@ColumnWidth(20)
	@ExcelProperty("修改时间")
	private Date updateTime;

	@ColumnWidth(20)
	@ExcelProperty("是否删除")
	private Integer isDeleted;

	@ColumnWidth(20)
	@ExcelProperty("租户ID")
	private String tenantId;
}
