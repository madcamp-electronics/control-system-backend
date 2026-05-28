package com.hanium.smart_drain.drain.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrainCreateResponse {

    private Long drainId;
    private String address;
    private LocalDateTime registeredAt;
}
