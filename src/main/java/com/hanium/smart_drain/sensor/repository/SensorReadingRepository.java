package com.hanium.smart_drain.sensor.repository;

import com.hanium.smart_drain.sensor.entity.SensorReading;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findByDrainIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
        Long drainId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );

    Optional<SensorReading> findFirstByDrainIdOrderByMeasuredAtDescReadingIdDesc(Long drainId);

    @Query(
        value = """
            SELECT DISTINCT ON (drain_id) *
            FROM sensor_readings
            ORDER BY drain_id, measured_at DESC, reading_id DESC
            """,
        nativeQuery = true
    )
    List<SensorReading> findLatestReadings();
}
