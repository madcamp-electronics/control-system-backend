package com.hanium.smart_drain.worker.controller;

import com.hanium.smart_drain.global.response.ApiResponse;
import com.hanium.smart_drain.worker.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("worker api ok");
    }
}
