package com.hanium.smart_drain.dashboard.service;

import com.hanium.smart_drain.alert.repository.AlertRepository;
import com.hanium.smart_drain.drain.repository.DrainRepository;
import com.hanium.smart_drain.sensor.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DrainRepository drainRepository;
    private final AlertRepository alertRepository;
    private final SensorReadingRepository sensorReadingRepository;

    // TODO: 지도 데이터, 요약 통계, 최근 알림 조회 로직 구현 예정
}
