package com.hanium.smart_drain.sensor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorPhotoUploadResponse {

    private String status;
    private String fileUrl;
}
