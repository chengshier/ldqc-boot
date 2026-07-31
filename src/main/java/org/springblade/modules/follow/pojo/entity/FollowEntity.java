package org.springblade.modules.follow.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

/**
 * 用户关注关系。
 *
 * <p>activeUniqueKey 在关系有效时保存“关注人:被关注人”，取消关注时置空。
 * 数据库唯一索引以此阻止并发重复关注，同时允许用户取消后重新关注。</p>
 */
@Data
@TableName("t_follow")
@Schema(description = "用户关注关系")
@EqualsAndHashCode(callSuper = true)
public class FollowEntity extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/** 关注发起用户ID。 */
	private Long uid;
	/** 被关注用户ID。 */
	private Long fid;
	/** 有效关系唯一键，取消关注后置空。 */
	private String activeUniqueKey;
}
