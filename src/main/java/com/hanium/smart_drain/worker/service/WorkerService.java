package com.hanium.smart_drain.worker.service;

import com.hanium.smart_drain.worker.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;

    // TODO: 작업자 등록 로직 구현 예정
    // TODO: 작업자 목록 조회 로직 구현 예정
    // TODO: 작업자 위치 업데이트 로직 구현 예정
    // TODO: 작업자 상태 변경 로직 구현 예정
}
