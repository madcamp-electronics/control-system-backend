package com.hanium.smart_drain.drain.repository;

import com.hanium.smart_drain.drain.entity.Drain;
import com.hanium.smart_drain.drain.entity.DrainStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrainRepository extends JpaRepository<Drain, Long> {
    List<Drain> findByStatus(DrainStatus status);
    long countByStatus(DrainStatus status);
}
