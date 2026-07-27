package org.springblade.modules.pointsbehavior.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorCommand implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String eventCode;
	private String bizType;
	private String bizId;
	private Long userId;
	private String requestId;
	private String source;
	@Builder.Default
	private Map<String, Object> ext = new HashMap<>();
}
