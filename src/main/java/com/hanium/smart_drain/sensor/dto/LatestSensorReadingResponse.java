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
public class LatestSensorReadingResponse {

    private Long drainId;
    /**
     * DB의 trash_level에 저장되는 값과 동일한 계산 수위입니다.
     * 계산식: 빗물받이 전체 높이 - 초음파 센서 감지 거리
     */
    private Double waterLevel;
    // 기존 API 소비자를 위한 호환 필드
    private Double trashLevel;
    private Double batteryLevel;
    private Integer signalStrength;
    private LocalDateTime measuredAt;
    private LocalDateTime receivedAt;
}
