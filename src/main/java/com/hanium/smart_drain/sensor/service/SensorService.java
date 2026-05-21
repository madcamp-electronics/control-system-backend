package com.hanium.smart_drain.sensor.service;

import com.hanium.smart_drain.sensor.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorReadingRepository sensorReadingRepository;

    // TODO: 센서 데이터 수신/저장/최근 측정값 조회 로직 구현 예정
}
