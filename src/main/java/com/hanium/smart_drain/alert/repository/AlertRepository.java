package com.hanium.smart_drain.alert.repository;

import com.hanium.smart_drain.alert.entity.Alert;
import com.hanium.smart_drain.alert.entity.AlertStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    boolean existsByDrainIdAndStatusIn(Long drainId, Collection<AlertStatus> statuses);
    List<Alert> findAllByOrderByCreatedAtDesc();
    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);
}
