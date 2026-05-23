package com.hanium.smart_drain.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceCompleteRequest {

    private String completionMemo;
}
