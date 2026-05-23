package com.hanium.smart_drain.maintenance.dto;

import com.hanium.smart_drain.maintenance.entity.MaintenancePhotoType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenancePhotoResponse {

    private Long id;
    private Long taskId;
    private Long fileId;
    private MaintenancePhotoType photoType;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}
