package com.hanium.smart_drain.maintenance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "maintenance_tasks")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MaintenanceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: 추후 필요 시 Drain/Alert/Worker와 @ManyToOne 연관관계로 변경 검토
    private Long drainId;
    private Long alertId;
    private Long assignedWorkerId;

    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    @Enumerated(EnumType.STRING)
    private MaintenancePriority priority;

    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
