package com.hanium.smart_drain.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerResponse {

    private Long workerId;
    private String name;
    private String phoneNumber;
}
