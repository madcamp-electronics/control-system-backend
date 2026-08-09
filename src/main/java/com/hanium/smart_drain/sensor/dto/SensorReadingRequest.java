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
    // 기존 MQTT 계약명. 실제 입력값은 초음파 센서가 측정한 수면까지의 거리(cm)입니다.
    private Double trashLevel;
    @NotNull
    private Double batteryLevel;
    @NotNull
    private Integer signalStrength;
    @NotNull
    private LocalDateTime measuredAt;
}
