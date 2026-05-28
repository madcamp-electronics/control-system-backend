package com.hanium.smart_drain.alert.service;

import com.hanium.smart_drain.alert.dto.AlertListResponse;
import com.hanium.smart_drain.alert.entity.Alert;
import com.hanium.smart_drain.alert.entity.AlertStatus;
import com.hanium.smart_drain.alert.entity.AlertType;
import com.hanium.smart_drain.alert.repository.AlertRepository;
import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.drain.repository.DrainRepository;
import com.hanium.smart_drain.global.exception.CustomException;
import com.hanium.smart_drain.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final DrainRepository drainRepository;

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

    // TODO: 알림 생성 로직 구현 예정
    // TODO: 알림 조회 로직 구현 예정
    // TODO: 알림 확인 처리 로직 구현 예정
    // TODO: 알림 해결 처리 로직 구현 예정
}
