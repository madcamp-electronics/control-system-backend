package com.hanium.smart_drain.risk.policy;

import com.hanium.smart_drain.alert.entity.AlertType;
import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.risk.dto.RiskAnalysisResult;
import com.hanium.smart_drain.risk.dto.RiskLevel;
import org.springframework.stereotype.Component;

@Component
public class DefaultRiskPolicy implements RiskPolicy {

    @Override
    public RiskAnalysisResult evaluate(
        Drain drain,
        Double waterLevel,
        Double trashLevel,
        Double batteryLevel,
        Integer signalStrength
    ) {
        if (drain == null) {
            return RiskAnalysisResult.builder()
                .riskLevel(RiskLevel.NORMAL)
                .message("drain not found")
                .needAlert(false)
                .needMaintenanceTask(false)
                .build();
        }

        Long drainId = drain.getId();

        // TODO: 기준값 상세 계산 및 정책 고도화
        if (isLowBattery(batteryLevel) || isInvalidSignal(signalStrength)) {
            return RiskAnalysisResult.builder()
                .drainId(drainId)
                .riskLevel(RiskLevel.SENSOR_ERROR)
                .alertType(AlertType.SENSOR_ERROR)
                .message("sensor health check required")
                .needAlert(true)
                .needMaintenanceTask(false)
                .build();
        }

        if (isAtOrAbove(waterLevel, drain.getDangerWaterLevel())) {
            return RiskAnalysisResult.builder()
                .drainId(drainId)
                .riskLevel(RiskLevel.FLOOD_RISK)
                .alertType(AlertType.FLOOD_RISK)
                .message("flood risk detected")
                .needAlert(true)
                .needMaintenanceTask(true)
                .build();
        }

        if (isAtOrAbove(trashLevel, drain.getDangerTrashLevel())) {
            return RiskAnalysisResult.builder()
                .drainId(drainId)
                .riskLevel(RiskLevel.NEED_INSPECTION)
                .alertType(AlertType.NEED_INSPECTION)
                .message("trash level exceeds danger threshold")
                .needAlert(true)
                .needMaintenanceTask(true)
                .build();
        }

        if (isAtOrAbove(waterLevel, drain.getWarningWaterLevel())) {
            return RiskAnalysisResult.builder()
                .drainId(drainId)
                .riskLevel(RiskLevel.NEED_INSPECTION)
                .alertType(AlertType.NEED_INSPECTION)
                .message("water level exceeds warning threshold")
                .needAlert(true)
                .needMaintenanceTask(true)
                .build();
        }

        return RiskAnalysisResult.builder()
            .drainId(drainId)
            .riskLevel(RiskLevel.NORMAL)
            .message("normal")
            .needAlert(false)
            .needMaintenanceTask(false)
            .build();
    }

    private boolean isAtOrAbove(Double value, Double threshold) {
        return value != null && threshold != null && value >= threshold;
    }

    private boolean isLowBattery(Double batteryLevel) {
        return batteryLevel != null && batteryLevel <= 10.0;
    }

    private boolean isInvalidSignal(Integer signalStrength) {
        return signalStrength != null && signalStrength <= 0;
    }
}
