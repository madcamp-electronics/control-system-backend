package com.hanium.smart_drain.sensor.entity;

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
    private Long id;

    // TODO: 추후 필요 시 Drain 엔티티와 @ManyToOne 연관관계로 변경 검토
    private Long drainId;

    private Double waterLevel;
    private Double trashLevel;
    private Double batteryLevel;
    private Integer signalStrength;
    private LocalDateTime measuredAt;
    private LocalDateTime receivedAt;
}
