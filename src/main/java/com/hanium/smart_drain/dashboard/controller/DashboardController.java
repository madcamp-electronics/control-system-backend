package com.hanium.smart_drain.dashboard.controller;

import com.hanium.smart_drain.dashboard.dto.DashboardMarkerResponse;
import com.hanium.smart_drain.dashboard.dto.DashboardSummaryResponse;
import com.hanium.smart_drain.dashboard.service.DashboardService;
import com.hanium.smart_drain.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/markers")
    public ApiResponse<List<DashboardMarkerResponse>> getMarkers() {
        return ApiResponse.success(dashboardService.getMarkers());
    }

    @GetMapping("/statistics")
    public ApiResponse<DashboardSummaryResponse> getStatistics() {
        return ApiResponse.success(dashboardService.getStatistics());
    }
}
