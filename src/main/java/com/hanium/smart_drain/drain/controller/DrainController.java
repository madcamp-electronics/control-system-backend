package com.hanium.smart_drain.drain.controller;

import com.hanium.smart_drain.drain.service.DrainService;
import com.hanium.smart_drain.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drains")
@RequiredArgsConstructor
public class DrainController {

    private final DrainService drainService;

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("drain api ok");
    }
}
