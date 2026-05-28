package com.hanium.smart_drain.alert.dto;

import com.hanium.smart_drain.alert.entity.AlertStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertStatusUpdateRequest {

    @NotNull
    private AlertStatus status;

    @NotNull
    private Long workerId;
}
