package org.springblade.modules.userbehaviorevent.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.userbehaviorevent.pojo.entity.UserBehaviorEventEntity;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserBehaviorEventDTO extends UserBehaviorEventEntity {
	@Serial
	private static final long serialVersionUID = 1L;
}
