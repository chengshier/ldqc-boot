package org.springblade.modules.pointsbehavior.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAwardResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String eventCode;
    private String requestId;
    private Integer matchedRuleCount;
    private Integer grantedRuleCount;
    private Integer grantedPoints;
    private String message;

    public static BehaviorAwardResult empty(String eventCode, String requestId, String message) {
        return BehaviorAwardResult.builder()
            .eventCode(eventCode)
            .requestId(requestId)
            .matchedRuleCount(0)
            .grantedRuleCount(0)
            .grantedPoints(0)
            .message(message)
            .build();
    }
}
