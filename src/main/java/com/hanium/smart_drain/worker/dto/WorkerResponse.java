package com.hanium.smart_drain.worker.dto;

import com.hanium.smart_drain.worker.entity.WorkerStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerResponse {

    private Long id;
    private String name;
    private String phoneNumber;
    private WorkerStatus status;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime createdAt;
}
