package com.hanium.smart_drain.worker.controller;

import com.hanium.smart_drain.global.response.ApiResponse;
import com.hanium.smart_drain.worker.dto.WorkerResponse;
import com.hanium.smart_drain.worker.service.WorkerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping
    public ApiResponse<List<WorkerResponse>> getWorkers() {
        return ApiResponse.success(workerService.getWorkers());
    }
}
