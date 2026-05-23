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
@Table(name = "maintenance_photos")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MaintenancePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: 추후 필요 시 MaintenanceTask/FileMetadata와 @ManyToOne 연관관계로 변경 검토
    private Long taskId;
    private Long fileId;

    @Enumerated(EnumType.STRING)
    private MaintenancePhotoType photoType;

    private LocalDateTime uploadedAt;
}
