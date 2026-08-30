package com.hanium.smart_drain.sensor.controller;

import com.hanium.smart_drain.global.response.ApiResponse;
import com.hanium.smart_drain.sensor.dto.SensorHistoryResponse;
import com.hanium.smart_drain.sensor.dto.LatestSensorReadingResponse;
import com.hanium.smart_drain.sensor.dto.SensorPhotoUploadResponse;
import com.hanium.smart_drain.sensor.service.SensorService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @GetMapping("/latest")
    public ApiResponse<List<LatestSensorReadingResponse>> getLatestReadings() {
        return ApiResponse.success(sensorService.getLatestReadings());
    }

    @GetMapping("/drains/{drainId}/latest")
    public ApiResponse<LatestSensorReadingResponse> getLatestReading(
        @PathVariable("drainId") Long drainId
    ) {
        return ApiResponse.success(sensorService.getLatestReading(drainId));
    }

    @PostMapping("/photos")
    public ResponseEntity<ApiResponse<SensorPhotoUploadResponse>> uploadPhoto(
        @RequestParam("drainId") Long drainId,
        @RequestParam("imageFile") MultipartFile imageFile
    ) {
        SensorPhotoUploadResponse response = sensorService.uploadDevicePhoto(drainId, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/drains/{drainId}/history")
    public ApiResponse<List<SensorHistoryResponse>> getDrainHistory(
        @PathVariable("drainId") Long drainId,
        @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return ApiResponse.success(sensorService.getDrainHistory(drainId, startTime, endTime));
    }
}
