package com.hanium.smart_drain.alert.repository;

import com.hanium.smart_drain.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}
