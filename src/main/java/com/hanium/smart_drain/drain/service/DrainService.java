package com.hanium.smart_drain.drain.service;

import com.hanium.smart_drain.drain.dto.DrainCreateRequest;
import com.hanium.smart_drain.drain.dto.DrainResponse;
import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.drain.entity.DrainStatus;
import com.hanium.smart_drain.drain.repository.DrainRepository;
import com.hanium.smart_drain.global.exception.CustomException;
import com.hanium.smart_drain.global.exception.ErrorCode;
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
    public List<DrainResponse> getDrains(String status) {
        List<Drain> drains;
        if (status == null || status.isBlank()) {
            drains = drainRepository.findAll();
        } else {
            DrainStatus drainStatus = parseDrainStatus(status);
            drains = drainRepository.findByStatus(drainStatus);
        }

        return drains.stream()
            .map(this::toResponse)
            .toList();
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
            .id(saved.getId())
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

    // TODO: 빗물받이 등록/조회 로직 구현 예정
    // TODO: 빗물받이 상태 변경 로직 구현 예정
    // TODO: 위험도 판단 기준값 관리 로직 구현 예정
}
