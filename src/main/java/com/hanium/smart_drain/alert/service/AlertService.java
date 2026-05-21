package com.hanium.smart_drain.alert.service;

import com.hanium.smart_drain.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    // TODO: 알림 생성/조회/확인 처리 로직 구현 예정
}
