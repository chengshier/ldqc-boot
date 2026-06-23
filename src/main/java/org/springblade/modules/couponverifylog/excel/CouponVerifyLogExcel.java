/**
 * 优惠券核销日志 Excel实体类
 */
package org.springblade.modules.couponverifylog.excel;

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
public class CouponVerifyLogExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;

	@ColumnWidth(20)
	@ExcelProperty("用户券ID")
	private Long userCouponId;

	@ColumnWidth(20)
	@ExcelProperty("用户ID")
	private Long userId;

	@ColumnWidth(20)
	@ExcelProperty("商家用户ID")
	private Long merchantUserId;

	@ColumnWidth(20)
	@ExcelProperty("券模板ID")
	private Long templateId;

	@ColumnWidth(20)
	@ExcelProperty("券号")
	private String couponNo;

	@ColumnWidth(20)
	@ExcelProperty("核销渠道")
	private String verifyChannel;

	@ColumnWidth(20)
	@ExcelProperty("核销结果")
	private Integer verifyResult;

	@ColumnWidth(20)
	@ExcelProperty("失败原因")
	private String failReason;

	@ColumnWidth(20)
	@ExcelProperty("状态")
	private String verifyStatus;

	@ColumnWidth(20)
	@ExcelProperty("订单号")
	private String orderNo;

	@ColumnWidth(20)
	@ExcelProperty("扩展JSON")
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
