package com.hanium.smart_drain.dashboard.dto;

import com.hanium.smart_drain.alert.entity.AlertStatus;
import com.hanium.smart_drain.alert.entity.AlertType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentAlertResponse {

    private Long id;
    private Long drainId;
    private AlertType type;
    private AlertStatus status;
    private String message;
    private LocalDateTime createdAt;
}
