package org.springblade.modules.usermessage.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_message")
public class UserMessage extends TenantEntity {
	private Long userId;
	private String messageType;
	private String title;
	private String content;
	private String bizType;
	private Long bizId;
	private String extraJson;
	private Byte readStatus;
}
