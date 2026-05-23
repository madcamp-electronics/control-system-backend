package com.hanium.smart_drain.worker.dto;

import com.hanium.smart_drain.maintenance.entity.MaintenancePriority;
import com.hanium.smart_drain.maintenance.entity.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerTaskResponse {

    private Long workerId;
    private Long taskId;
    private Long drainId;
    private MaintenanceStatus maintenanceStatus;
    private MaintenancePriority priority;
}
