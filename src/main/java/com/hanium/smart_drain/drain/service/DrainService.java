package com.hanium.smart_drain.drain.service;

import com.hanium.smart_drain.alert.repository.AlertRepository;
import com.hanium.smart_drain.drain.dto.DrainCreateRequest;
import com.hanium.smart_drain.drain.dto.DrainCreateResponse;
import com.hanium.smart_drain.drain.dto.DrainListResponse;
import com.hanium.smart_drain.drain.dto.DrainResponse;
import com.hanium.smart_drain.drain.dto.DrainUpdateRequest;
import com.hanium.smart_drain.drain.dto.DrainUpdateResponse;
import com.hanium.smart_drain.drain.dto.DrainWorkPhotoResponse;
import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.drain.entity.DrainStatus;
import com.hanium.smart_drain.drain.repository.DrainRepository;
import com.hanium.smart_drain.global.exception.CustomException;
import com.hanium.smart_drain.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DrainService {

    private final DrainRepository drainRepository;
    private final AlertRepository alertRepository;

    @Transactional
    public DrainCreateResponse createDrain(DrainCreateRequest request) {
        Drain drain = Drain.builder()
            .address(request.getAddress())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .status(DrainStatus.NORMAL)
            .totalDepth(request.getTotalDepth())
            .trashLevelThreshold(request.getTrashLevelThreshold())
            .latestDevicePhotoUrl(null)
            .registeredAt(LocalDateTime.now())
            .build();

        Drain saved = drainRepository.save(drain);
        return DrainCreateResponse.builder()
            .drainId(saved.getId())
            .address(saved.getAddress())
            .registeredAt(saved.getRegisteredAt())
            .build();
    }

    @Transactional(readOnly = true)
    public List<DrainListResponse> getDrains(String status) {
        List<Drain> drains;
        if (status == null || status.isBlank()) {
            drains = drainRepository.findAll();
        } else {
            DrainStatus drainStatus = parseDrainStatus(status);
            drains = drainRepository.findByStatus(drainStatus);
        }

        return drains.stream()
            .map(this::toListResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DrainResponse getDrainById(Long drainId) {
        Drain drain = drainRepository.findById(drainId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "drain not found"));
        return toResponse(drain, true);
    }

    @Transactional
    public DrainUpdateResponse updateDrain(Long drainId, DrainUpdateRequest request) {
        Drain drain = drainRepository.findById(drainId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "drain not found"));

        drain.updateInfo(
            request.getAddress(),
            request.getTrashLevelThreshold()
        );

        return DrainUpdateResponse.builder()
            .drainId(drain.getId())
            .trashLevelThreshold(drain.getTrashLevelThreshold())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private DrainStatus parseDrainStatus(String status) {
        try {
            return DrainStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "invalid status: " + status);
        }
    }

    private DrainResponse toResponse(Drain saved) {
        return toResponse(saved, false);
    }

    private DrainResponse toResponse(Drain saved, boolean includeWorkPhotos) {
        return DrainResponse.builder()
            .drainId(saved.getId())
            .address(saved.getAddress())
            .latitude(saved.getLatitude())
            .longitude(saved.getLongitude())
            .status(saved.getStatus())
            .totalDepth(saved.getTotalDepth())
            .trashLevelThreshold(saved.getTrashLevelThreshold())
            .latestDevicePhotoUrl(saved.getLatestDevicePhotoUrl())
            .workPhotos(includeWorkPhotos ? getWorkPhotos(saved.getId()) : List.of())
            .build();
    }

    private DrainListResponse toListResponse(Drain drain) {
        return DrainListResponse.builder()
            .drainId(drain.getId())
            .address(drain.getAddress())
            .latitude(drain.getLatitude())
            .longitude(drain.getLongitude())
            .status(drain.getStatus())
            .totalDepth(drain.getTotalDepth())
            .build();
    }

    private List<DrainWorkPhotoResponse> getWorkPhotos(Long drainId) {
        return alertRepository.findByDrainIdOrderByCreatedAtDesc(drainId).stream()
            .filter(alert -> alert.getBeforePhotoUrl() != null && alert.getAfterPhotoUrl() != null)
            .findFirst()
            .map(alert -> List.of(DrainWorkPhotoResponse.builder()
                .alertId(alert.getId())
                .status(alert.getStatus())
                .beforePhotoUrl(alert.getBeforePhotoUrl())
                .afterPhotoUrl(alert.getAfterPhotoUrl())
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .build()))
            .orElseGet(List::of);
    }

    // TODO: 빗물받이 등록/조회 로직 구현 예정
    // TODO: 빗물받이 상태 변경 로직 구현 예정
    // TODO: 위험도 판단 기준값 관리 로직 구현 예정
}
