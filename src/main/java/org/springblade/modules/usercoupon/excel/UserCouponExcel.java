/**
 * 用户优惠券 Excel实体类
 */
package org.springblade.modules.usercoupon.excel;

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
public class UserCouponExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;

	@ColumnWidth(20)
	@ExcelProperty("用户ID")
	private Long userId;

	@ColumnWidth(20)
	@ExcelProperty("券模板ID")
	private Long couponTemplateId;

	@ColumnWidth(20)
	@ExcelProperty("券号")
	private String couponNo;

	@ColumnWidth(20)
	@ExcelProperty("券状态")
	private String couponStatus;

	@ColumnWidth(20)
	@ExcelProperty("剩余时长(分钟)")
	private Integer remainDurationMinutes;

	@ColumnWidth(20)
	@ExcelProperty("剩余次数")
	private Integer remainTimes;

	@ColumnWidth(20)
	@ExcelProperty("有效期开始时间")
	private Date validStartAt;

	@ColumnWidth(20)
	@ExcelProperty("有效期结束时间")
	private Date validEndAt;

	@ColumnWidth(20)
	@ExcelProperty("锁定订单号")
	private String lockedOrderNo;

	@ColumnWidth(20)
	@ExcelProperty("使用订单号")
	private String usedOrderNo;

	@ColumnWidth(20)
	@ExcelProperty("使用时间")
	private Date usedAt;

	@ColumnWidth(20)
	@ExcelProperty("核销商家用户ID")
	private Long verifyMerchantUserId;

	@ColumnWidth(20)
	@ExcelProperty("核销时间")
	private Date verifyAt;

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
