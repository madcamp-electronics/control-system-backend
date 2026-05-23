package com.hanium.smart_drain.maintenance.repository;

import com.hanium.smart_drain.maintenance.entity.MaintenanceTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {
}
