package com.hanium.smart_drain.alert.service;

import com.hanium.smart_drain.alert.dto.AlertListResponse;
import com.hanium.smart_drain.alert.dto.AlertCompleteResponse;
import com.hanium.smart_drain.alert.dto.AlertPhotoType;
import com.hanium.smart_drain.alert.dto.AlertStatusUpdateRequest;
import com.hanium.smart_drain.alert.dto.AlertStatusUpdateResponse;
import com.hanium.smart_drain.alert.entity.Alert;
import com.hanium.smart_drain.alert.entity.AlertStatus;
import com.hanium.smart_drain.alert.entity.AlertType;
import com.hanium.smart_drain.alert.repository.AlertRepository;
import com.hanium.smart_drain.auth.entity.User;
import com.hanium.smart_drain.auth.entity.UserRole;
import com.hanium.smart_drain.auth.repository.UserRepository;
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
    private final UserRepository userRepository;
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
                    .workerId(alert.getWorkerId())
                    .address(drain != null ? drain.getAddress() : null)
                    .latitude(drain != null ? drain.getLatitude() : null)
                    .longitude(drain != null ? drain.getLongitude() : null)
                    .riskLevel(alert.getRiskLevel())
                    .status(alert.getStatus())
                    .beforePhotoUrl(alert.getBeforePhotoUrl())
                    .afterPhotoUrl(alert.getAfterPhotoUrl())
                    .createdAt(alert.getCreatedAt())
                    .updatedAt(alert.getUpdatedAt())
                    .resolvedAt(alert.getResolvedAt())
                    .build();
            })
            .toList();
    }

    @Transactional
    public AlertStatusUpdateResponse updateAlertStatus(
        Long alertId,
        AlertStatusUpdateRequest request,
        Long authenticatedWorkerId
    ) {
        if (request.getStatus() != AlertStatus.PROCESSING) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "status must be PROCESSING");
        }

        validateWorker(authenticatedWorkerId);

        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "alert not found"));
        if (alert.getStatus() != AlertStatus.ACTIVE) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "only active alerts can be accepted");
        }

        LocalDateTime now = LocalDateTime.now();
        alert.updateStatusAndWorker(request.getStatus(), authenticatedWorkerId, now);

        return AlertStatusUpdateResponse.builder()
            .alertId(alert.getId())
            .status(alert.getStatus())
            .workerId(alert.getWorkerId())
            .updatedAt(alert.getUpdatedAt())
            .build();
    }

    @Transactional
    public AlertStatusUpdateResponse assignAlert(Long alertId, Long workerId) {
        validateWorker(workerId);

        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "alert not found"));
        if (alert.getStatus() != AlertStatus.ACTIVE) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "only active alerts can be assigned");
        }

        LocalDateTime now = LocalDateTime.now();
        alert.updateStatusAndWorker(AlertStatus.PROCESSING, workerId, now);

        return AlertStatusUpdateResponse.builder()
            .alertId(alert.getId())
            .status(alert.getStatus())
            .workerId(alert.getWorkerId())
            .updatedAt(alert.getUpdatedAt())
            .build();
    }

    private void validateWorker(Long workerId) {
        User worker = userRepository.findById(workerId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "worker not found"));
        if (worker.getRole() != UserRole.ROLE_WORKER) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "selected user is not a worker");
        }
    }

    @Transactional
    public AlertCompleteResponse completeAlertWithPhotos(
        Long alertId,
        MultipartFile beforeImageFile,
        MultipartFile afterImageFile,
        Long authenticatedWorkerId
    ) {
        validateImageFile(beforeImageFile, "beforeImageFile");
        validateImageFile(afterImageFile, "afterImageFile");

        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "alert not found"));
        validateAssignedWorker(alert, authenticatedWorkerId);

        Drain drain = drainRepository.findById(alert.getDrainId())
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "drain not found"));

        Path storagePath = Path.of(alertStorageDir);
        Path beforeTarget = storagePath.resolve(
            buildStoredFileName(alertId, AlertPhotoType.BEFORE, beforeImageFile.getOriginalFilename())
        );
        Path afterTarget = storagePath.resolve(
            buildStoredFileName(alertId, AlertPhotoType.AFTER, afterImageFile.getOriginalFilename())
        );

        try {
            Files.createDirectories(storagePath);
            Files.copy(beforeImageFile.getInputStream(), beforeTarget, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(afterImageFile.getInputStream(), afterTarget, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            deleteQuietly(beforeTarget);
            deleteQuietly(afterTarget);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "failed to store work photos");
        }

        LocalDateTime now = LocalDateTime.now();
        alert.updateBeforePhoto(alertUrlPrefix + "/" + beforeTarget.getFileName(), now);
        alert.updateAfterPhoto(alertUrlPrefix + "/" + afterTarget.getFileName(), now);
        alert.complete(now);
        drain.updateStatus(DrainStatus.NORMAL);

        return AlertCompleteResponse.builder()
            .alertId(alert.getId())
            .drainId(alert.getDrainId())
            .status(alert.getStatus())
            .resolvedAt(alert.getResolvedAt())
            .build();
    }

    private void validateImageFile(MultipartFile imageFile, String fieldName) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, fieldName + " is required");
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best-effort cleanup after a failed multipart write.
        }
    }

    private void validateAssignedWorker(Alert alert, Long authenticatedWorkerId) {
        if (alert.getStatus() != AlertStatus.PROCESSING) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "alert must be processing");
        }
        if (!authenticatedWorkerId.equals(alert.getWorkerId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "alert is assigned to another worker");
        }
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
