package com.hanium.smart_drain.risk.policy;

import com.hanium.smart_drain.alert.entity.AlertType;
import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.risk.dto.RiskAnalysisResult;
import com.hanium.smart_drain.risk.dto.RiskLevel;
import org.springframework.stereotype.Component;

@Component
public class DefaultRiskPolicy implements RiskPolicy {

    private static final int MIN_USABLE_WIFI_RSSI_DBM = -90;

    @Override
    public RiskAnalysisResult evaluate(
        Drain drain,
        Double trashLevel,
        Double coverDistance,
        Double batteryLevel,
        Integer signalStrength
    ) {
        if (drain == null) {
            return RiskAnalysisResult.builder()
                .riskLevel(RiskLevel.NORMAL)
                .message("drain not found")
                .needAlert(false)
                .build();
        }

        Long drainId = drain.getId();

        // TODO: 기준값 상세 계산 및 정책 고도화
        if (isInvalidTrashLevel(trashLevel)
            || isInvalidCoverDistance(coverDistance)
            || isLowBattery(batteryLevel)
            || isInvalidSignal(signalStrength)) {
            return RiskAnalysisResult.builder()
                .drainId(drainId)
                .riskLevel(RiskLevel.SENSOR_ERROR)
                .alertType(AlertType.SENSOR_ERROR)
                .message("sensor health check required")
                .needAlert(true)
                .build();
        }

        if (isCoverBlocked(coverDistance, drain.getCoverDistanceThreshold())) {
            return RiskAnalysisResult.builder()
                .drainId(drainId)
                .riskLevel(RiskLevel.NEED_INSPECTION)
                .alertType(AlertType.NEED_INSPECTION)
                .message("drain cover blocked")
                .needAlert(true)
                .build();
        }

        if (isAtOrAbove(trashLevel, drain.getTrashLevelThreshold())) {
            return RiskAnalysisResult.builder()
                .drainId(drainId)
                .riskLevel(RiskLevel.NEED_INSPECTION)
                .alertType(AlertType.NEED_INSPECTION)
                .message("trash level exceeds threshold")
                .needAlert(true)
                .build();
        }

        return RiskAnalysisResult.builder()
            .drainId(drainId)
            .riskLevel(RiskLevel.NORMAL)
            .message("normal")
            .needAlert(false)
            .build();
    }

    private boolean isAtOrAbove(Double value, Double threshold) {
        return value != null && threshold != null && value >= threshold;
    }

    private boolean isCoverBlocked(Double coverDistance, Double coverDistanceThreshold) {
        return coverDistance != null
            && coverDistanceThreshold != null
            && coverDistance <= coverDistanceThreshold;
    }

    private boolean isInvalidTrashLevel(Double trashLevel) {
        return trashLevel != null && trashLevel < 0;
    }

    private boolean isInvalidCoverDistance(Double coverDistance) {
        return coverDistance != null && coverDistance < 0;
    }

    private boolean isLowBattery(Double batteryLevel) {
        return batteryLevel != null && batteryLevel <= 10.0;
    }

    private boolean isInvalidSignal(Integer signalStrength) {
        // ESP32 WiFi.RSSI() returns dBm values, where normal signals are negative.
        return signalStrength != null && signalStrength <= MIN_USABLE_WIFI_RSSI_DBM;
    }
}
