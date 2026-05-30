package com.hanium.smart_drain.drain.dto;

import com.hanium.smart_drain.drain.entity.DrainStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrainResponse {

    private Long drainId;
    private String address;
    private Double latitude;
    private Double longitude;
    private DrainStatus status;
    private Double totalDepth;
    private Double trashLevelThreshold;
    private String latestDevicePhotoUrl;
    private List<DrainWorkPhotoResponse> workPhotos;
}
