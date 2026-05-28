package com.hanium.smart_drain.alert.entity;

import jakarta.persistence.Column;
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
@Table(name = "alerts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long id;

    @Column(name = "drain_id", nullable = false)
    private Long drainId;

    @Column(name = "worker_id")
    private Long workerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private AlertType riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(name = "before_photo_url")
    private String beforePhotoUrl;

    @Column(name = "after_photo_url")
    private String afterPhotoUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public void updateStatusAndWorker(AlertStatus status, Long workerId, LocalDateTime updatedAt) {
        this.status = status;
        this.workerId = workerId;
        this.updatedAt = updatedAt;
    }

    public void updateBeforePhoto(String beforePhotoUrl, LocalDateTime updatedAt) {
        this.beforePhotoUrl = beforePhotoUrl;
        this.updatedAt = updatedAt;
    }

    public void updateAfterPhoto(String afterPhotoUrl, LocalDateTime updatedAt) {
        this.afterPhotoUrl = afterPhotoUrl;
        this.updatedAt = updatedAt;
    }

    public void complete(LocalDateTime resolvedAt) {
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = resolvedAt;
        this.updatedAt = resolvedAt;
    }
}
