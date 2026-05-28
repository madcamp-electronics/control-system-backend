package com.hanium.smart_drain.sensor.dto;

import com.hanium.smart_drain.risk.dto.RiskLevel;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorReadingIngestResponse {

    private Long readingId;
    private Long drainId;
    private RiskLevel riskLevel;
    private LocalDateTime receivedAt;
}
