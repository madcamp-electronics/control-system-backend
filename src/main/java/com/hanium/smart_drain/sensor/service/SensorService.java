package com.hanium.smart_drain.sensor.service;

import com.hanium.smart_drain.alert.service.AlertService;
import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.drain.entity.DrainStatus;
import com.hanium.smart_drain.drain.repository.DrainRepository;
import com.hanium.smart_drain.global.exception.CustomException;
import com.hanium.smart_drain.global.exception.ErrorCode;
import com.hanium.smart_drain.global.storage.S3StorageService;
import com.hanium.smart_drain.risk.dto.RiskAnalysisResult;
import com.hanium.smart_drain.risk.dto.RiskLevel;
import com.hanium.smart_drain.risk.service.RiskAnalysisService;
import com.hanium.smart_drain.sensor.dto.SensorHistoryResponse;
import com.hanium.smart_drain.sensor.dto.SensorReadingIngestResponse;
import com.hanium.smart_drain.sensor.dto.SensorPhotoUploadResponse;
import com.hanium.smart_drain.sensor.dto.SensorReadingRequest;
import com.hanium.smart_drain.sensor.entity.SensorReading;
import com.hanium.smart_drain.sensor.repository.SensorReadingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorReadingRepository sensorReadingRepository;
    private final DrainRepository drainRepository;
    private final RiskAnalysisService riskAnalysisService;
    private final AlertService alertService;
    private final S3StorageService s3StorageService;

    @Transactional
    public SensorReadingIngestResponse ingestReading(SensorReadingRequest request) {
        Drain drain = drainRepository.findById(request.getDrainId())
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "drain not found"));

        LocalDateTime receivedAt = LocalDateTime.now();
        SensorReading sensorReading = SensorReading.builder()
            .drainId(request.getDrainId())
            .waterLevel(request.getWaterLevel())
            .trashLevel(request.getTrashLevel())
            .batteryLevel(request.getBatteryLevel())
            .signalStrength(request.getSignalStrength())
            .measuredAt(request.getMeasuredAt())
            .receivedAt(receivedAt)
            .build();
        SensorReading saved = sensorReadingRepository.save(sensorReading);

        RiskAnalysisResult riskResult = riskAnalysisService.analyze(
            request.getDrainId(),
            request.getWaterLevel(),
            request.getTrashLevel(),
            request.getBatteryLevel(),
            request.getSignalStrength()
        );

        if (riskResult.isNeedAlert() && riskResult.getAlertType() != null) {
            alertService.createActiveAlert(drain.getId(), riskResult.getAlertType());
        }

        updateDrainStatusByRiskLevel(drain, riskResult.getRiskLevel());

        return SensorReadingIngestResponse.builder()
            .readingId(saved.getReadingId())
            .drainId(saved.getDrainId())
            .riskLevel(riskResult.getRiskLevel())
            .receivedAt(saved.getReceivedAt())
            .build();
    }

    @Transactional
    public SensorPhotoUploadResponse uploadDevicePhoto(Long drainId, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "imageFile is required");
        }

        Drain drain = drainRepository.findById(drainId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "drain not found"));

        String storedFileName = buildStoredFileName(drainId, imageFile.getOriginalFilename());
        String fileUrl = s3StorageService.upload(imageFile, "device/" + storedFileName);
        drain.updateLatestDevicePhotoUrl(fileUrl);

        return SensorPhotoUploadResponse.builder()
            .status("SUCCESS")
            .fileUrl(fileUrl)
            .build();
    }

    @Transactional(readOnly = true)
    public List<SensorHistoryResponse> getDrainHistory(
        Long drainId,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        if (startTime == null || endTime == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "startTime and endTime are required");
        }
        if (startTime.isAfter(endTime)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "startTime must be before endTime");
        }

        drainRepository.findById(drainId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "drain not found"));

        return sensorReadingRepository
            .findByDrainIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(drainId, startTime, endTime)
            .stream()
            .map(reading -> SensorHistoryResponse.builder()
                .waterLevel(reading.getWaterLevel())
                .trashLevel(reading.getTrashLevel())
                .batteryLevel(reading.getBatteryLevel())
                .measuredAt(reading.getMeasuredAt())
                .build())
            .toList();
    }

    // TODO: 센서 데이터 수신/저장 로직 구현 예정
    // TODO: 저장 후 RiskAnalysisService를 통해 위험도 판단 예정
    // TODO: 최신 측정값 조회 로직 구현 예정

    private void updateDrainStatusByRiskLevel(Drain drain, RiskLevel riskLevel) {
        if (riskLevel == null) {
            return;
        }
        DrainStatus targetStatus = switch (riskLevel) {
            case FLOOD_RISK -> DrainStatus.FLOOD_RISK;
            case NEED_INSPECTION, SENSOR_ERROR -> DrainStatus.NEED_INSPECTION;
            case NORMAL -> DrainStatus.NORMAL;
        };
        if (drain.getStatus() != targetStatus) {
            drain.updateStatus(targetStatus);
        }
    }

    private String buildStoredFileName(Long drainId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "drain_" + drainId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + extension;
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
}
