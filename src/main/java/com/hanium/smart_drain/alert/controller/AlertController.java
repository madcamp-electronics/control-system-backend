package com.hanium.smart_drain.alert.controller;

import com.hanium.smart_drain.alert.service.AlertService;
import com.hanium.smart_drain.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("alert api ok");
    }
}
