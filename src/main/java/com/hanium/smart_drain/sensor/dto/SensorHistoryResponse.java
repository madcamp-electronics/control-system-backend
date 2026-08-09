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

    private Double waterLevel;
    // 기존 API 소비자를 위한 호환 필드
    private Double trashLevel;
    private Double batteryLevel;
    private LocalDateTime measuredAt;
}
