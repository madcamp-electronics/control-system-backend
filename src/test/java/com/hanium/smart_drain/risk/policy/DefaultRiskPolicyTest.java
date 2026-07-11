package com.hanium.smart_drain.risk.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.hanium.smart_drain.alert.entity.AlertType;
import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.drain.entity.DrainStatus;
import com.hanium.smart_drain.risk.dto.RiskAnalysisResult;
import com.hanium.smart_drain.risk.dto.RiskLevel;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DefaultRiskPolicyTest {

    private final DefaultRiskPolicy policy = new DefaultRiskPolicy();

    @Test
    void normalWifiRssiDoesNotCreateSensorError() {
        RiskAnalysisResult result = policy.evaluate(testDrain(), 20.0, 80.0, -55);

        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.NORMAL);
        assertThat(result.isNeedAlert()).isFalse();
    }

    @Test
    void veryWeakWifiRssiCreatesSensorError() {
        RiskAnalysisResult result = policy.evaluate(testDrain(), 20.0, 80.0, -91);

        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.SENSOR_ERROR);
        assertThat(result.getAlertType()).isEqualTo(AlertType.SENSOR_ERROR);
        assertThat(result.isNeedAlert()).isTrue();
    }

    @Test
    void negativeTrashLevelCreatesSensorError() {
        RiskAnalysisResult result = policy.evaluate(testDrain(), -1.0, 80.0, -55);

        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.SENSOR_ERROR);
        assertThat(result.getAlertType()).isEqualTo(AlertType.SENSOR_ERROR);
        assertThat(result.isNeedAlert()).isTrue();
    }

    private Drain testDrain() {
        return Drain.builder()
            .id(1L)
            .address("test address")
            .latitude(37.0)
            .longitude(127.0)
            .status(DrainStatus.NORMAL)
            .totalDepth(100.0)
            .trashLevelThreshold(80.0)
            .registeredAt(LocalDateTime.now())
            .build();
    }
}
