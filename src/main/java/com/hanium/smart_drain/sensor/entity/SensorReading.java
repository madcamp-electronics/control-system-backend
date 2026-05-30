package com.hanium.smart_drain.sensor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sensor_readings")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_id")
    private Long readingId;

    // TODO: 추후 필요 시 Drain 엔티티와 @ManyToOne 연관관계로 변경 검토
    @Column(name = "drain_id", nullable = false)
    private Long drainId;

    @Column(nullable = false)
    private Double trashLevel;
    @Column(nullable = false)
    private Double batteryLevel;
    @Column(nullable = false)
    private Integer signalStrength;
    @Column(nullable = false)
    private LocalDateTime measuredAt;
    @Column(nullable = false)
    private LocalDateTime receivedAt;
}
