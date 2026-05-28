package com.hanium.smart_drain.drain.controller;

import com.hanium.smart_drain.drain.dto.DrainCreateRequest;
import com.hanium.smart_drain.drain.dto.DrainListResponse;
import com.hanium.smart_drain.drain.dto.DrainResponse;
import com.hanium.smart_drain.drain.service.DrainService;
import com.hanium.smart_drain.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drains")
@RequiredArgsConstructor
public class DrainController {

    private final DrainService drainService;

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("drain api ok");
    }

    @GetMapping
    public ApiResponse<List<DrainListResponse>> getDrains(
        @RequestParam(value = "status", required = false) String status
    ) {
        return ApiResponse.success(drainService.getDrains(status));
    }

    @GetMapping("/{drainId}")
    public ApiResponse<DrainResponse> getDrainDetail(
        @PathVariable("drainId") Long drainId
    ) {
        return ApiResponse.success(drainService.getDrainById(drainId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DrainResponse>> createDrain(
        @Valid @RequestBody DrainCreateRequest request
    ) {
        DrainResponse response = drainService.createDrain(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
