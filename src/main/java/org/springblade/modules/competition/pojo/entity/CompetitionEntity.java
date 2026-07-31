package org.springblade.modules.competition.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 赛事主表。
 *
 * <p>赛事信息由管理端维护，用户端只能读取已开放赛事。报名时间、费用和人数限制
 * 均以后端字段为准，不能相信小程序传入的价格、支付状态或剩余名额。</p>
 */
@Data
@TableName("ldqc_competition")
@Schema(description = "赛事")
@EqualsAndHashCode(callSuper = true)
public class CompetitionEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String title;
	private String coverImage;
	private Date startTime;
	private Date endTime;
	private String location;
	private String address;
	private BigDecimal longitude;
	private BigDecimal latitude;

	/** 报名开放时间，为空时不限制开始时间。 */
	private Date signupStartTime;
	/** 报名截止时间；为空时默认赛事开始前均可报名。 */
	private Date signupEndTime;

	/** 已确认或已占用名额人数。 */
	private Integer participantCount;
	/** 人数上限，0或空表示不限制。 */
	private Integer maxParticipants;
	/** 单个报名订单允许的人数上限。 */
	private Integer maxPeoplePerOrder;

	/** 单人报名费用，人民币元。 */
	private BigDecimal price;
	/** 支付方式 FREE/WECHAT。 */
	private String paymentMode;
	/** 报名规则或免责声明。 */
	private String signupNotice;
	private String description;

	/** 赛事状态：0草稿、1报名中、2进行中、3已结束、4已下架。 */
	private Integer status;
}
