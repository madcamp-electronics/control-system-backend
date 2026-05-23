package com.hanium.smart_drain.worker.repository;

import com.hanium.smart_drain.worker.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
}
