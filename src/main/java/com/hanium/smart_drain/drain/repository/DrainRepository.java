package com.hanium.smart_drain.drain.repository;

import com.hanium.smart_drain.drain.entity.Drain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrainRepository extends JpaRepository<Drain, Long> {
}
