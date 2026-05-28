package com.hanium.smart_drain.sensor.repository;

import com.hanium.smart_drain.sensor.entity.SensorReading;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findByDrainIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
        Long drainId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );
}
