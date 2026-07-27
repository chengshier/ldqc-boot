package org.springblade.modules.competitionsignup.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 赛事报名订单。
 *
 * <p>本表保存报名时赛事快照。赛事后续修改标题、地点、费用或时间时，不改变历史订单。
 * activeUniqueKey 只在订单占用名额期间有值，用数据库唯一索引防止并发重复报名。</p>
 */
@Data
@TableName("ldqc_competition_signup")
@Schema(description = "赛事报名订单")
@EqualsAndHashCode(callSuper = true)
public class CompetitionSignupEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	private String orderNo;
	private String requestId;
	private String activeUniqueKey;
	private Long competitionId;
	private Long userId;

	private String competitionTitle;
	private String competitionCover;
	private Date competitionStartTime;
	private Date competitionEndTime;
	private String competitionLocation;
	private String competitionAddress;

	private String signupName;
	private String phone;
	private String idCard;
	private String teamName;
	private Integer numPeople;

	/** 单人费用快照，人民币元。 */
	private BigDecimal unitPrice;
	/** 订单应付金额，人民币元。 */
	private BigDecimal totalAmount;
	/** FREE/WECHAT。 */
	private String paymentMode;
	/** 0未支付、1已支付或免费、2已退款。 */
	private Integer payStatus;
	/** PENDING_PAYMENT/CONFIRMED/CANCELLED/EXPIRED/REFUND_PENDING/REFUNDED。 */
	private String orderStatus;
	private String paymentOrderNo;
	private Date paymentExpireTime;
	private Date paidAt;
	private Date signupTime;
	private Date cancelledAt;
	private String cancelReason;
	private String remark;
}
