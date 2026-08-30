package com.hanium.smart_drain.sensor.controller;

import com.hanium.smart_drain.global.response.ApiResponse;
import com.hanium.smart_drain.sensor.dto.SensorHistoryResponse;
import com.hanium.smart_drain.sensor.dto.LatestSensorReadingResponse;
import com.hanium.smart_drain.sensor.dto.SensorPhotoUploadResponse;
import com.hanium.smart_drain.sensor.dto.SensorReadingIngestResponse;
import com.hanium.smart_drain.sensor.dto.SensorReadingRequest;
import com.hanium.smart_drain.sensor.service.SensorService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 장치가 측정값을 직접 올리는 경로. MQTT 브로커를 거치지 않는다.
     *
     * SensorMqttSubscriber 와 같은 SensorService.ingestReading 을 호출하므로
     * 저장/위험도 판정/알림 생성 동작은 MQTT 경로와 완전히 동일하다.
     * 브로커를 띄울 수 없는 장치(LTE 모듈 없이 붙는 시연용 장비 등)를 위한 것.
     */
    @PostMapping("/readings")
    public ResponseEntity<ApiResponse<SensorReadingIngestResponse>> ingestReading(
        @Valid @RequestBody SensorReadingRequest request
    ) {
        SensorReadingIngestResponse response = sensorService.ingestReading(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
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
