package com.hanium.smart_drain.maintenance.service;

import com.hanium.smart_drain.file.service.FileService;
import com.hanium.smart_drain.maintenance.repository.MaintenancePhotoRepository;
import com.hanium.smart_drain.maintenance.repository.MaintenanceTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final MaintenancePhotoRepository maintenancePhotoRepository;
    private final FileService fileService;

    // TODO: Alert 기반 작업 자동 생성 로직 구현 예정
    // TODO: 작업자 배정 로직 구현 예정
    // TODO: 작업 시작 처리 로직 구현 예정
    // TODO: 수리 전/후 사진 연결 로직 구현 예정
    // TODO: 작업 완료 처리 로직 구현 예정
    // TODO: 작업 완료 시 Drain 상태 NORMAL 변경 예정
    // TODO: 작업 완료 시 Alert 상태 RESOLVED 변경 예정
}
