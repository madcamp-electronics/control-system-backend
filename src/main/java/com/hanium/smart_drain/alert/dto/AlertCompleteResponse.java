package com.hanium.smart_drain.alert.dto;

import com.hanium.smart_drain.alert.entity.AlertStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertCompleteResponse {

    private Long alertId;
    private Long drainId;
    private AlertStatus status;
    private LocalDateTime resolvedAt;
}
