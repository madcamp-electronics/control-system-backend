package com.hanium.smart_drain.drain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "drains")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Drain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drain_id")
    private Long id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DrainStatus status;

    @Column(nullable = false)
    private Double totalDepth;

    @Column(name = "trash_level_threshold", nullable = false)
    private Double trashLevelThreshold;

    @Column(name = "latest_device_photo_url")
    private String latestDevicePhotoUrl;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    public void updateStatus(DrainStatus status) {
        this.status = status;
    }

    public void updateLatestDevicePhotoUrl(String latestDevicePhotoUrl) {
        this.latestDevicePhotoUrl = latestDevicePhotoUrl;
    }

    public void updateInfo(String address, Double trashLevelThreshold) {
        this.address = address;
        this.trashLevelThreshold = trashLevelThreshold;
    }
}
