package com.hanium.smart_drain.alert.dto;

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
public class AlertResponse {

    private Long id;
    private Long drainId;
    private AlertType type;
    private String message;
    private Boolean acknowledged;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
}
