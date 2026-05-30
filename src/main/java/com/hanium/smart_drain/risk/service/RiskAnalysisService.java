package com.hanium.smart_drain.risk.service;

import com.hanium.smart_drain.alert.service.AlertService;
import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.drain.repository.DrainRepository;
import com.hanium.smart_drain.risk.dto.RiskAnalysisResult;
import com.hanium.smart_drain.risk.policy.DefaultRiskPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskAnalysisService {

    private final DrainRepository drainRepository;
    private final DefaultRiskPolicy defaultRiskPolicy;
    private final AlertService alertService;

    public RiskAnalysisResult analyze(
        Long drainId,
        Double trashLevel,
        Double batteryLevel,
        Integer signalStrength
    ) {
        // TODO: drainId로 Drain 기준값 조회
        // TODO: DefaultRiskPolicy로 위험도 판단
        // TODO: 위험 시 Alert 생성
        // TODO: Drain 상태 변경
        Drain drain = drainRepository.findById(drainId).orElse(null);
        return defaultRiskPolicy.evaluate(drain, trashLevel, batteryLevel, signalStrength);
    }
}
