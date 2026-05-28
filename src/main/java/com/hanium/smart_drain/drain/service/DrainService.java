package com.hanium.smart_drain.drain.service;

import com.hanium.smart_drain.drain.dto.DrainCreateRequest;
import com.hanium.smart_drain.drain.dto.DrainListResponse;
import com.hanium.smart_drain.drain.dto.DrainResponse;
import com.hanium.smart_drain.drain.dto.DrainUpdateRequest;
import com.hanium.smart_drain.drain.dto.DrainUpdateResponse;
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

    @Transactional
    public DrainResponse createDrain(DrainCreateRequest request) {
        Drain drain = Drain.builder()
            .address(request.getAddress())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .status(DrainStatus.NORMAL)
            .totalDepth(request.getTotalDepth())
            .waterLevelThreshold(request.getWaterLevelThreshold())
            .trashLevelThreshold(request.getTrashLevelThreshold())
            .latestDevicePhotoUrl(null)
            .build();

        Drain saved = drainRepository.save(drain);
        return toResponse(saved);
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
        return toResponse(drain);
    }

    @Transactional
    public DrainUpdateResponse updateDrain(Long drainId, DrainUpdateRequest request) {
        Drain drain = drainRepository.findById(drainId)
            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "drain not found"));

        drain.updateInfo(
            request.getAddress(),
            request.getWaterLevelThreshold(),
            request.getTrashLevelThreshold()
        );

        return DrainUpdateResponse.builder()
            .drainId(drain.getId())
            .waterLevelThreshold(drain.getWaterLevelThreshold())
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
        return DrainResponse.builder()
            .drainId(saved.getId())
            .address(saved.getAddress())
            .latitude(saved.getLatitude())
            .longitude(saved.getLongitude())
            .status(saved.getStatus())
            .totalDepth(saved.getTotalDepth())
            .waterLevelThreshold(saved.getWaterLevelThreshold())
            .trashLevelThreshold(saved.getTrashLevelThreshold())
            .latestDevicePhotoUrl(saved.getLatestDevicePhotoUrl())
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

    // TODO: 빗물받이 등록/조회 로직 구현 예정
    // TODO: 빗물받이 상태 변경 로직 구현 예정
    // TODO: 위험도 판단 기준값 관리 로직 구현 예정
}
