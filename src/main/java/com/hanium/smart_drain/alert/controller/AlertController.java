package com.hanium.smart_drain.alert.controller;

import com.hanium.smart_drain.alert.dto.AlertListResponse;
import com.hanium.smart_drain.alert.dto.AlertCompleteResponse;
import com.hanium.smart_drain.alert.dto.AlertPhotoType;
import com.hanium.smart_drain.alert.dto.AlertPhotoUploadResponse;
import com.hanium.smart_drain.alert.dto.AlertStatusUpdateRequest;
import com.hanium.smart_drain.alert.dto.AlertStatusUpdateResponse;
import com.hanium.smart_drain.alert.service.AlertService;
import com.hanium.smart_drain.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ApiResponse<List<AlertListResponse>> getAlerts(
        @RequestParam(value = "status", required = false) String status
    ) {
        return ApiResponse.success(alertService.getAlerts(status));
    }

    @PatchMapping("/{alertId}/status")
    public ApiResponse<AlertStatusUpdateResponse> updateAlertStatus(
        @PathVariable("alertId") Long alertId,
        @Valid @RequestBody AlertStatusUpdateRequest request
    ) {
        return ApiResponse.success(alertService.updateAlertStatus(alertId, request));
    }

    @PostMapping("/{alertId}/photos")
    public ResponseEntity<ApiResponse<AlertPhotoUploadResponse>> uploadAlertPhoto(
        @PathVariable("alertId") Long alertId,
        @RequestParam("imageFile") MultipartFile imageFile,
        @RequestParam("photoType") AlertPhotoType photoType
    ) {
        AlertPhotoUploadResponse response = alertService.uploadAlertPhoto(alertId, imageFile, photoType);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PatchMapping("/{alertId}/complete")
    public ApiResponse<AlertCompleteResponse> completeAlert(
        @PathVariable("alertId") Long alertId
    ) {
        return ApiResponse.success(alertService.completeAlert(alertId));
    }
}
