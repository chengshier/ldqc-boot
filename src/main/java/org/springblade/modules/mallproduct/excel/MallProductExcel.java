/**
 * 积分商城商品 Excel实体类
 */
package org.springblade.modules.mallproduct.excel;

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
public class MallProductExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@ColumnWidth(20)
	@ExcelProperty("主键")
	private Long id;

	@ColumnWidth(20)
	@ExcelProperty("商品编码")
	private String productCode;

	@ColumnWidth(20)
	@ExcelProperty("商品名称")
	private String productName;

	@ColumnWidth(20)
	@ExcelProperty("商品描述")
	private String productDesc;

	@ColumnWidth(20)
	@ExcelProperty("商品类型")
	private String productType;

	@ColumnWidth(20)
	@ExcelProperty("商品封面图")
	private String coverUrl;

	@ColumnWidth(20)
	@ExcelProperty("兑换所需绿豆")
	private Integer salePoints;

	@ColumnWidth(20)
	@ExcelProperty("市场价(分)")
	private Integer marketAmount;

	@ColumnWidth(20)
	@ExcelProperty("库存总量")
	private Integer stockTotal;

	@ColumnWidth(20)
	@ExcelProperty("可用库存")
	private Integer stockAvailable;

	@ColumnWidth(20)
	@ExcelProperty("状态")
	private Integer status;

	@ColumnWidth(20)
	@ExcelProperty("排序号")
	private Integer sortNo;

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
