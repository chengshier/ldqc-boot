/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 * <p>
 * Use of this software is governed by the Commercial License Agreement
 * obtained after purchasing a license from BladeX.
 * <p>
 * 1. This software is for development use only under a valid license
 * from BladeX.
 * <p>
 * 2. Redistribution of this software's source code to any third party
 * without a commercial license is strictly prohibited.
 * <p>
 * 3. Licensees may copyright their own code but cannot use segments
 * from this software for such purposes. Copyright of this software
 * remains with BladeX.
 * <p>
 * Using this software signifies agreement to this License, and the software
 * must not be used for illegal purposes.
 * <p>
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY. The author is
 * not liable for any claims arising from secondary or illegal development.
 * <p>
 * Author: Chill Zhuang (bladejava@qq.com)
 */
package org.springblade.modules.sportinvite.excel;


import lombok.Data;

import java.util.Date;
import java.math.BigDecimal;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import java.io.Serializable;
import java.io.Serial;


/**
 * 运动邀约表 Excel实体类
 *
 * @author BladeX
 * @since 2026-05-21
 */
@Data
@ColumnWidth(25)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class SportInviteExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("主键ID")
	private Long id;
	/**
	 * 租户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("租户ID")
	private String tenantId;
	/**
	 * 是否删除
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否删除")
	private Integer isDeleted;
	/**
	 * 邀约状态：DRAFT草稿 OPEN报名中 FULL已满员 ENDED已结束 CANCELED已取消 OFFLINE平台下架
	 */
	@ColumnWidth(20)
	@ExcelProperty("邀约状态：DRAFT草稿 OPEN报名中 FULL已满员 ENDED已结束 CANCELED已取消 OFFLINE平台下架")
	private String inviteStatus;
	/**
	 * 发起人用户ID
	 */
	@ColumnWidth(20)
	@ExcelProperty("发起人用户ID")
	private Long publisherUserId;
	/**
	 * 运动类型：BADMINTON羽毛球 BASKETBALL篮球 FOOTBALL足球 TENNIS网球 RUNNING跑步 CYCLING骑行 FITNESS健身 OTHER其他
	 */
	@ColumnWidth(20)
	@ExcelProperty("运动类型：BADMINTON羽毛球 BASKETBALL篮球 FOOTBALL足球 TENNIS网球 RUNNING跑步 CYCLING骑行 FITNESS健身 OTHER其他")
	private String sportType;
	/**
	 * 活动标题
	 */
	@ColumnWidth(20)
	@ExcelProperty("活动标题")
	private String title;
	/**
	 * 开始时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("开始时间")
	private Date startTime;
	/**
	 * 结束时间
	 */
	@ColumnWidth(20)
	@ExcelProperty("结束时间")
	private Date endTime;
	/**
	 * 场馆名称
	 */
	@ColumnWidth(20)
	@ExcelProperty("场馆名称")
	private String venueName;
	/**
	 * 详细地址
	 */
	@ColumnWidth(20)
	@ExcelProperty("详细地址")
	private String addressDetail;
	/**
	 * 经度
	 */
	@ColumnWidth(20)
	@ExcelProperty("经度")
	private BigDecimal longitude;
	/**
	 * 纬度
	 */
	@ColumnWidth(20)
	@ExcelProperty("纬度")
	private BigDecimal latitude;
	/**
	 * 费用方式：AA AA制 ORGANIZER发起人承担 FREE免费 SELF_PAY到场自付
	 */
	@ColumnWidth(20)
	@ExcelProperty("费用方式：AA AA制 ORGANIZER发起人承担 FREE免费 SELF_PAY到场自付")
	private String feeType;
	/**
	 * 当前已通过人数
	 */
	@ColumnWidth(20)
	@ExcelProperty("当前已通过人数")
	private Integer currentPeople;
	/**
	 * 目标人数
	 */
	@ColumnWidth(20)
	@ExcelProperty("目标人数")
	private Integer targetPeople;
	/**
	 * 性别限制：UNLIMITED不限 MALE男 FEMALE女
	 */
	@ColumnWidth(20)
	@ExcelProperty("性别限制：UNLIMITED不限 MALE男 FEMALE女")
	private String genderLimit;
	/**
	 * 最小年龄
	 */
	@ColumnWidth(20)
	@ExcelProperty("最小年龄")
	private Integer ageMin;
	/**
	 * 最大年龄
	 */
	@ColumnWidth(20)
	@ExcelProperty("最大年龄")
	private Integer ageMax;
	/**
	 * 水平要求：NEWBIE新手 BEGINNER入门 INTERMEDIATE进阶 EXPERT高手
	 */
	@ColumnWidth(20)
	@ExcelProperty("水平要求：NEWBIE新手 BEGINNER入门 INTERMEDIATE进阶 EXPERT高手")
	private String levelLimit;
	/**
	 * 参与要求
	 */
	@ColumnWidth(20)
	@ExcelProperty("参与要求")
	private String requirement;
	/**
	 * 是否需要审核：1是 0否
	 */
	@ColumnWidth(20)
	@ExcelProperty("是否需要审核：1是 0否")
	private Integer needAudit;
	/**
	 * 发起人运动水平
	 */
	@ColumnWidth(20)
	@ExcelProperty("发起人运动水平")
	private String organizerLevel;
	/**
	 * 发起人运动频率
	 */
	@ColumnWidth(20)
	@ExcelProperty("发起人运动频率")
	private String organizerFrequency;
	/**
	 * 发起人简短介绍
	 */
	@ColumnWidth(20)
	@ExcelProperty("发起人简短介绍")
	private String organizerIntro;
	/**
	 * 发起人手机号
	 */
	@ColumnWidth(20)
	@ExcelProperty("发起人手机号")
	private String contactPhone;
	/**
	 * 发起人微信号
	 */
	@ColumnWidth(20)
	@ExcelProperty("发起人微信号")
	private String contactWechat;
	/**
	 * 联系方式可见规则：APPROVED_ONLY通过后可见
	 */
	@ColumnWidth(20)
	@ExcelProperty("联系方式可见规则：APPROVED_ONLY通过后可见")
	private String contactVisibleRule;
	/**
	 * 活动说明
	 */
	@ColumnWidth(20)
	@ExcelProperty("活动说明")
	private String description;
	/**
	 * 注意事项
	 */
	@ColumnWidth(20)
	@ExcelProperty("注意事项")
	private String notice;
	/**
	 * 封面图
	 */
	@ColumnWidth(20)
	@ExcelProperty("封面图")
	private String coverImage;

}
