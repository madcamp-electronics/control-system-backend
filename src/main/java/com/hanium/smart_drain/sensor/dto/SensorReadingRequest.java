package com.hanium.smart_drain.sensor.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorReadingRequest {

    @NotNull
    private Long drainId;
    @NotNull
    private Double waterLevel;
    @NotNull
    private Double trashLevel;
    @NotNull
    private Double batteryLevel;
    @NotNull
    private Integer signalStrength;
    @NotNull
    private LocalDateTime measuredAt;
}
