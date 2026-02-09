package org.springblade.modules.imgDetail.excel;

import lombok.Data;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;

/**
 * 图片详情表 Excel实体类
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class ImgDetailExcel implements Serializable {

@Serial
private static final long serialVersionUID = 1L;

@ColumnWidth(20)
@ExcelProperty("主键")
private Long id;

@ColumnWidth(20)
@ExcelProperty("租户ID")
private String tenantId;

@ColumnWidth(20)
@ExcelProperty("内容描述")
private String content;

@ColumnWidth(20)
@ExcelProperty("封面")
private String cover;

@ColumnWidth(20)
@ExcelProperty("用户ID")
private Long userId;

@ColumnWidth(20)
@ExcelProperty("分类ID")
private Long categoryId;

@ColumnWidth(20)
@ExcelProperty("父分类ID")
private Long categoryPid;

@ColumnWidth(20)
@ExcelProperty("图片地址列表")
private String imgsUrl;

@ColumnWidth(20)
@ExcelProperty("图片数量")
private Integer count;

@ColumnWidth(20)
@ExcelProperty("排序")
private Integer sort;

@ColumnWidth(20)
@ExcelProperty("浏览数")
private Long viewCount;

@ColumnWidth(20)
@ExcelProperty("点赞数")
private Long agreeCount;

@ColumnWidth(20)
@ExcelProperty("收藏数")
private Long collectionCount;

@ColumnWidth(20)
@ExcelProperty("评论数")
private Long commentCount;

@ColumnWidth(20)
@ExcelProperty("是否已删除")
private Integer isDeleted;
}