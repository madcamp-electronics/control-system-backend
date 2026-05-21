package com.hanium.smart_drain.drain.dto;

import com.hanium.smart_drain.drain.entity.DrainStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrainMapResponse {

    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private DrainStatus status;
}
