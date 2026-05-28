package com.hanium.smart_drain.alert.service;

import com.hanium.smart_drain.alert.dto.AlertListResponse;
import com.hanium.smart_drain.alert.dto.AlertCompleteResponse;
import com.hanium.smart_drain.alert.dto.AlertPhotoType;
import com.hanium.smart_drain.alert.dto.AlertPhotoUploadResponse;
import com.hanium.smart_drain.alert.dto.AlertStatusUpdateRequest;
import com.hanium.smart_drain.alert.dto.AlertStatusUpdateResponse;
import com.hanium.smart_drain.alert.entity.Alert;
import com.hanium.smart_drain.alert.entity.AlertStatus;
import com.hanium.smart_drain.alert.entity.AlertType;
import com.hanium.smart_drain.alert.repository.AlertRepository;
import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.drain.entity.DrainStatus;
import com.hanium.smart_drain.drain.repository.DrainRepository;
import com.hanium.smart_drain.global.exception.CustomException;
import com.hanium.smart_drain.global.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final DrainRepository drainRepository;
    @Value("${app.storage.alert-dir:storage/alerts}")
    private String alertStorageDir;
    @Value("${app.storage.alert-url-prefix:/storage/alerts}")
    private String alertUrlPrefix;

    public Alert createActiveAlert(Long drainId, AlertType alertType) {
        boolean alreadyActive = alertRepository.existsByDrainIdAndStatusIn(
            drainId,
            List.of(AlertStatus.ACTIVE, AlertStatus.PROCESSING)
        );
        if (alreadyActive) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        Alert alert = Alert.builder()
            .drainId(drainId)
            .riskLevel(alertType)
            .status(AlertStatus.ACTIVE)
            .createdAt(now)
            .updatedAt(now)
            .build();
        return alertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public List<AlertListResponse> getAlerts(String status) {
        List<Alert> alerts;
        if (status == null || status.isBlank()) {
            alerts = alertRepository.findAllByOrderByCreatedAtDesc();
        } else {
            alerts = alertRepository.findByStatusOrderByCreatedAtDesc(parseAlertStatus(status));
        }

        Map<Long, Drain> drainMap = buildDrainMap(alerts);
        return alerts.stream()
            .map(alert -> {
                Drain drain = drainMap.get(alert.getDrainId());
                return AlertListResponse.builder()
                    .alertId(alert.getId())
                    .drainId(alert.getDrainId())
                    .address(drain != null ? drain.getAddress() : null)
                    .latitude(drain != null ? drain.getLatitude() : null)
                    .longitude(drain != null ? drain.getLongitude() : null)
                    .riskLevel(alert.getRiskLevel())
                    .status(alert.getStatus())
                    .createdAt(alert.getCreatedAt())
                    .build();
            })
            .toList();
    }

    @Transactional
    public AlertStatusUpdateResponse updateAlertStatus(Long alertId, AlertStatusUpdateRequest request) {
        if (request.getStatus() != AlertStatus.PROCESSING) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "status must be PROCESSING");
        }

        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "alert not found"));

        LocalDateTime now = LocalDateTime.now();
        alert.updateStatusAndWorker(request.getStatus(), request.getWorkerId(), now);

        return AlertStatusUpdateResponse.builder()
            .alertId(alert.getId())
            .status(alert.getStatus())
            .workerId(alert.getWorkerId())
            .updatedAt(alert.getUpdatedAt())
            .build();
    }

    @Transactional
    public AlertPhotoUploadResponse uploadAlertPhoto(Long alertId, MultipartFile imageFile, AlertPhotoType photoType) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "imageFile is required");
        }
        if (photoType == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "photoType is required");
        }

        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "alert not found"));

        String storedFileName = buildStoredFileName(alertId, photoType, imageFile.getOriginalFilename());
        Path storagePath = Path.of(alertStorageDir);
        Path targetPath = storagePath.resolve(storedFileName);
        try {
            Files.createDirectories(storagePath);
            Files.copy(imageFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "failed to store image file");
        }

        String fileUrl = alertUrlPrefix + "/" + storedFileName;
        LocalDateTime now = LocalDateTime.now();
        if (photoType == AlertPhotoType.BEFORE) {
            alert.updateBeforePhoto(fileUrl, now);
        } else {
            alert.updateAfterPhoto(fileUrl, now);
        }

        return AlertPhotoUploadResponse.builder()
            .photoId(alert.getId())
            .alertId(alert.getId())
            .fileUrl(fileUrl)
            .photoType(photoType)
            .build();
    }

    @Transactional
    public AlertCompleteResponse completeAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "alert not found"));

        Drain drain = drainRepository.findById(alert.getDrainId())
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "drain not found"));

        LocalDateTime now = LocalDateTime.now();
        alert.complete(now);
        drain.updateStatus(DrainStatus.NORMAL);

        return AlertCompleteResponse.builder()
            .alertId(alert.getId())
            .drainId(alert.getDrainId())
            .status(alert.getStatus())
            .resolvedAt(alert.getResolvedAt())
            .build();
    }

    private AlertStatus parseAlertStatus(String status) {
        try {
            return AlertStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "invalid status: " + status);
        }
    }

    private Map<Long, Drain> buildDrainMap(List<Alert> alerts) {
        List<Long> drainIds = alerts.stream()
            .map(Alert::getDrainId)
            .distinct()
            .toList();
        Map<Long, Drain> drainMap = new HashMap<>();
        drainRepository.findAllById(drainIds).forEach(drain -> drainMap.put(drain.getId(), drain));
        return drainMap;
    }

    private String buildStoredFileName(Long alertId, AlertPhotoType photoType, String originalFilename) {
        String suffix = photoType.name().toLowerCase(Locale.ROOT);
        String extension = extractExtension(originalFilename);
        return "alert_" + alertId + "_" + suffix + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (ext.length() > 10) {
            return ".jpg";
        }
        return ext;
    }

    // TODO: 알림 생성 로직 구현 예정
    // TODO: 알림 조회 로직 구현 예정
    // TODO: 알림 확인 처리 로직 구현 예정
    // TODO: 알림 해결 처리 로직 구현 예정
}
