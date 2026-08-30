package com.hanium.smart_drain.alert.dto;

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
public class AlertListResponse {

    private Long alertId;
    private Long drainId;
    private Long workerId;
    private String address;
    private Double latitude;
    private Double longitude;
    private AlertType riskLevel;
    private AlertStatus status;
    private String beforePhotoUrl;
    private String afterPhotoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
