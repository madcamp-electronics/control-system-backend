package com.hanium.smart_drain.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerCreateRequest {

    private String name;
    private String phoneNumber;
}
