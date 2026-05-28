package com.hanium.smart_drain.alert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertPhotoUploadResponse {

    private Long photoId;
    private Long alertId;
    private String fileUrl;
    private AlertPhotoType photoType;
}
