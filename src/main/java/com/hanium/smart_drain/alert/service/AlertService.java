package com.hanium.smart_drain.alert.service;

import com.hanium.smart_drain.alert.entity.Alert;
import com.hanium.smart_drain.alert.entity.AlertStatus;
import com.hanium.smart_drain.alert.entity.AlertType;
import com.hanium.smart_drain.alert.repository.AlertRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

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

    // TODO: 알림 생성 로직 구현 예정
    // TODO: 알림 조회 로직 구현 예정
    // TODO: 알림 확인 처리 로직 구현 예정
    // TODO: 알림 해결 처리 로직 구현 예정
}
