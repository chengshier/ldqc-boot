package org.springblade.modules.userbehaviorevent.excel;

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
public class UserBehaviorEventExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@ExcelProperty("主键")
	private Long id;
	@ExcelProperty("行为事件编码")
	private String eventCode;
	@ExcelProperty("用户ID")
	private Long userId;
	@ExcelProperty("业务类型")
	private String bizType;
	@ExcelProperty("业务对象ID")
	private String bizId;
	@ExcelProperty("事件状态")
	private Integer eventStatus;
	@ExcelProperty("请求幂等号")
	private String requestId;
	@ExcelProperty("来源")
	private String source;
	@ExcelProperty("行为发生时间")
	private Date eventTime;
	@ExcelProperty("扩展JSON")
	private String extJson;
}
