package com.hanium.smart_drain.dashboard.dto;

import com.hanium.smart_drain.drain.dto.DrainMapResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMapResponse {

    private List<DrainMapResponse> drains;
}
