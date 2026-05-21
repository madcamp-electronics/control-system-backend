package com.hanium.smart_drain.dashboard.controller;

import com.hanium.smart_drain.dashboard.service.DashboardService;
import com.hanium.smart_drain.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("dashboard api ok");
    }
}
