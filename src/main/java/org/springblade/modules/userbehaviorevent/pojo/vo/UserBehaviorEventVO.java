package org.springblade.modules.userbehaviorevent.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.userbehaviorevent.pojo.entity.UserBehaviorEventEntity;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "UserBehaviorEventVO对象")
public class UserBehaviorEventVO extends UserBehaviorEventEntity {
	@Serial
	private static final long serialVersionUID = 1L;
}
