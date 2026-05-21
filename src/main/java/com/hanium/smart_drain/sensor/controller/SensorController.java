package com.hanium.smart_drain.sensor.controller;

import com.hanium.smart_drain.global.response.ApiResponse;
import com.hanium.smart_drain.sensor.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("sensor api ok");
    }
}
