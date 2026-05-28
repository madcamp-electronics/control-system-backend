package com.hanium.smart_drain.drain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrainUpdateRequest {

    @NotBlank
    private String address;

    @NotNull
    private Double waterLevelThreshold;

    @NotNull
    private Double trashLevelThreshold;
}
