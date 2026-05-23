package com.hanium.smart_drain.maintenance.repository;

import com.hanium.smart_drain.maintenance.entity.MaintenancePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenancePhotoRepository extends JpaRepository<MaintenancePhoto, Long> {
}
