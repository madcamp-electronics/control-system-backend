package com.hanium.smart_drain.drain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrainCreateRequest {

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double totalDepth;
    private Double warningWaterLevel;
    private Double dangerWaterLevel;
    private Double warningTrashLevel;
    private Double dangerTrashLevel;
}
