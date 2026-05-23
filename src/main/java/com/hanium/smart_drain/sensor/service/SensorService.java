package com.hanium.smart_drain.sensor.service;

import com.hanium.smart_drain.risk.service.RiskAnalysisService;
import com.hanium.smart_drain.sensor.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorReadingRepository sensorReadingRepository;
    private final RiskAnalysisService riskAnalysisService;

    // TODO: 센서 데이터 수신/저장 로직 구현 예정
    // TODO: 저장 후 RiskAnalysisService를 통해 위험도 판단 예정
    // TODO: 최신 측정값 조회 로직 구현 예정
}
