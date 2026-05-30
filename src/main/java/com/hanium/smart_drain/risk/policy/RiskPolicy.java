package com.hanium.smart_drain.risk.policy;

import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.risk.dto.RiskAnalysisResult;

public interface RiskPolicy {

    RiskAnalysisResult evaluate(
        Drain drain,
        Double trashLevel,
        Double batteryLevel,
        Integer signalStrength
    );
}
