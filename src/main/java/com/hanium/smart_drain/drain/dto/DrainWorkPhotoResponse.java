package com.hanium.smart_drain.drain.dto;

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
public class DrainWorkPhotoResponse {

    private Long alertId;
    private AlertStatus status;
    private String beforePhotoUrl;
    private String afterPhotoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
