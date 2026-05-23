package com.hanium.smart_drain.risk.dto;

import com.hanium.smart_drain.alert.entity.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAnalysisResult {

    private Long drainId;
    private RiskLevel riskLevel;
    private AlertType alertType;
    private String message;
    private boolean needAlert;
    private boolean needMaintenanceTask;
}
