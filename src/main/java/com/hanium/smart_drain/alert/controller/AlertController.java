package com.hanium.smart_drain.alert.controller;

import com.hanium.smart_drain.alert.dto.AlertListResponse;
import com.hanium.smart_drain.alert.service.AlertService;
import com.hanium.smart_drain.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("alert api ok");
    }

    @GetMapping
    public ApiResponse<List<AlertListResponse>> getAlerts(
        @RequestParam(value = "status", required = false) String status
    ) {
        return ApiResponse.success(alertService.getAlerts(status));
    }
}
