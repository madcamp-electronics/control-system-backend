package com.hanium.smart_drain.dashboard.dto;

import com.hanium.smart_drain.maintenance.entity.MaintenancePriority;
import com.hanium.smart_drain.maintenance.entity.MaintenanceStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentMaintenanceTaskResponse {

    private Long id;
    private Long drainId;
    private Long alertId;
    private Long assignedWorkerId;
    private MaintenanceStatus status;
    private MaintenancePriority priority;
    private LocalDateTime createdAt;
}
