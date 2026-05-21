package com.hanium.smart_drain.sensor.repository;

import com.hanium.smart_drain.sensor.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
}
