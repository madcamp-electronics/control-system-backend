package com.hanium.smart_drain.maintenance.controller;

import com.hanium.smart_drain.global.response.ApiResponse;
import com.hanium.smart_drain.maintenance.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance/tasks")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // TODO: GET /api/maintenance/tasks
    // TODO: GET /api/maintenance/tasks/{id}
    // TODO: PATCH /api/maintenance/tasks/{id}/assign
    // TODO: PATCH /api/maintenance/tasks/{id}/start
    // TODO: POST /api/maintenance/tasks/{id}/photos/before
    // TODO: POST /api/maintenance/tasks/{id}/photos/after
    // TODO: PATCH /api/maintenance/tasks/{id}/complete

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("maintenance api ok");
    }
}
