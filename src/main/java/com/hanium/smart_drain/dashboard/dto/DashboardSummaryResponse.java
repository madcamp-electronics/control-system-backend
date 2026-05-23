package com.hanium.smart_drain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private Long totalDrainCount;
    private Long normalCount;
    private Long needInspectionCount;
    private Long floodRiskCount;
    private Long underMaintenanceCount;
    private Long activeAlertCount;
    private Long pendingTaskCount;
    private Long inProgressTaskCount;
}
