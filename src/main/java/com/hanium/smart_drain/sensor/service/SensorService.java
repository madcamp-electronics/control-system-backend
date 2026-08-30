package com.hanium.smart_drain.sensor.service;

import com.hanium.smart_drain.alert.service.AlertService;
import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.drain.entity.DrainStatus;
import com.hanium.smart_drain.drain.repository.DrainRepository;
import com.hanium.smart_drain.global.exception.CustomException;
import com.hanium.smart_drain.global.exception.ErrorCode;
import com.hanium.smart_drain.risk.dto.RiskAnalysisResult;
import com.hanium.smart_drain.risk.dto.RiskLevel;
import com.hanium.smart_drain.risk.service.RiskAnalysisService;
import com.hanium.smart_drain.sensor.dto.SensorHistoryResponse;
import com.hanium.smart_drain.sensor.dto.LatestSensorReadingResponse;
import com.hanium.smart_drain.sensor.dto.SensorReadingIngestResponse;
import com.hanium.smart_drain.sensor.dto.SensorPhotoUploadResponse;
import com.hanium.smart_drain.sensor.dto.SensorReadingRequest;
import com.hanium.smart_drain.sensor.entity.SensorReading;
import com.hanium.smart_drain.sensor.repository.SensorReadingRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${app.storage.device-dir:storage/device}")
    private String deviceStorageDir;
    @Value("${app.storage.device-url-prefix:/storage/device}")
    private String deviceUrlPrefix;

    @Transactional
    public SensorReadingIngestResponse ingestReading(SensorReadingRequest request) {
        Drain drain = drainRepository.findById(request.getDrainId())
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "drain not found"));
        // 기존 컬럼명은 유지하지만 저장값의 의미는 계산된 수위입니다.
        // waterLevel = drain.totalDepth - ultrasonicDistance
        Double trashLevel = calculateWaterLevelFromDistance(drain, request.getTrashLevel());

        LocalDateTime receivedAt = LocalDateTime.now();
        SensorReading sensorReading = SensorReading.builder()
            .drainId(request.getDrainId())
            .trashLevel(trashLevel)
            .coverDistance(request.getCoverDistance())
            .batteryLevel(request.getBatteryLevel())
            .signalStrength(request.getSignalStrength())
            .measuredAt(request.getMeasuredAt())
            .receivedAt(receivedAt)
            .build();
        SensorReading saved = sensorReadingRepository.save(sensorReading);

        RiskAnalysisResult riskResult = riskAnalysisService.analyze(
            request.getDrainId(),
            trashLevel,
            request.getCoverDistance(),
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
        Path storagePath = Path.of(deviceStorageDir);
        Path targetPath = storagePath.resolve(storedFileName);

        try {
            Files.createDirectories(storagePath);
            Files.copy(imageFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "failed to store image file");
        }

        String fileUrl = deviceUrlPrefix + "/" + storedFileName;
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
                .waterLevel(reading.getTrashLevel())
                .trashLevel(reading.getTrashLevel())
                .coverDistance(reading.getCoverDistance())
                .batteryLevel(reading.getBatteryLevel())
                .measuredAt(reading.getMeasuredAt())
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public LatestSensorReadingResponse getLatestReading(Long drainId) {
        drainRepository.findById(drainId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "drain not found"));

        SensorReading reading = sensorReadingRepository
            .findFirstByDrainIdOrderByMeasuredAtDescReadingIdDesc(drainId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "sensor reading not found"));

        return toLatestResponse(reading);
    }

    @Transactional(readOnly = true)
    public List<LatestSensorReadingResponse> getLatestReadings() {
        return sensorReadingRepository.findLatestReadings().stream()
            .map(this::toLatestResponse)
            .toList();
    }

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

    private LatestSensorReadingResponse toLatestResponse(SensorReading reading) {
        return LatestSensorReadingResponse.builder()
            .drainId(reading.getDrainId())
            .waterLevel(reading.getTrashLevel())
            .trashLevel(reading.getTrashLevel())
            .batteryLevel(reading.getBatteryLevel())
            .signalStrength(reading.getSignalStrength())
            .measuredAt(reading.getMeasuredAt())
            .receivedAt(reading.getReceivedAt())
            .build();
    }

    private Double calculateWaterLevelFromDistance(Drain drain, Double distanceCm) {
        if (distanceCm == null) {
            return null;
        }
        if (distanceCm < 0) {
            return distanceCm;
        }

        Double totalDepth = drain.getTotalDepth();
        if (totalDepth == null || totalDepth <= 0) {
            return distanceCm;
        }

        double waterLevel = totalDepth - distanceCm;
        if (waterLevel < 0) {
            return 0.0;
        }
        if (waterLevel > totalDepth) {
            return totalDepth;
        }
        return waterLevel;
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
