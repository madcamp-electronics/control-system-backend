package com.hanium.smart_drain.sensor.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorHistoryResponse {

    private Double trashLevel;
    private Double coverDistance;
    private Double batteryLevel;
    private LocalDateTime measuredAt;
}
