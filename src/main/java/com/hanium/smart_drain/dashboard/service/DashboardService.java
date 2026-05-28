package com.hanium.smart_drain.dashboard.service;

import com.hanium.smart_drain.alert.repository.AlertRepository;
import com.hanium.smart_drain.alert.entity.AlertStatus;
import com.hanium.smart_drain.dashboard.dto.DashboardMarkerResponse;
import com.hanium.smart_drain.dashboard.dto.DashboardSummaryResponse;
import com.hanium.smart_drain.drain.entity.DrainStatus;
import com.hanium.smart_drain.drain.repository.DrainRepository;
import com.hanium.smart_drain.sensor.repository.SensorReadingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DrainRepository drainRepository;
    private final AlertRepository alertRepository;
    private final SensorReadingRepository sensorReadingRepository;

    @Transactional(readOnly = true)
    public List<DashboardMarkerResponse> getMarkers() {
        return drainRepository.findAll().stream()
            .map(drain -> DashboardMarkerResponse.builder()
                .drainId(drain.getId())
                .latitude(drain.getLatitude())
                .longitude(drain.getLongitude())
                .status(drain.getStatus())
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getStatistics() {
        long totalDrainCount = drainRepository.count();
        long normalCount = drainRepository.countByStatus(DrainStatus.NORMAL);
        long needInspectionCount = drainRepository.countByStatus(DrainStatus.NEED_INSPECTION);
        long floodRiskCount = drainRepository.countByStatus(DrainStatus.FLOOD_RISK);
        long activeAlertCount = alertRepository.countByStatus(AlertStatus.ACTIVE);
        long processingAlertCount = alertRepository.countByStatus(AlertStatus.PROCESSING);

        return DashboardSummaryResponse.builder()
            .totalDrainCount(totalDrainCount)
            .normalCount(normalCount)
            .needInspectionCount(needInspectionCount)
            .floodRiskCount(floodRiskCount)
            .activeAlertCount(activeAlertCount)
            .processingAlertCount(processingAlertCount)
            .build();
    }

    // TODO: 지도 마커 데이터 조회 로직 구현 예정
    // TODO: 상태별 빗물받이 개수 조회 로직 구현 예정
    // TODO: 최근 알림 조회 로직 구현 예정
    // TODO: 처리중 경보 목록 조회 로직 구현 예정
}
