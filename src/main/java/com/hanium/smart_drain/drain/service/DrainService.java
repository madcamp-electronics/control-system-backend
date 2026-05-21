package com.hanium.smart_drain.drain.service;

import com.hanium.smart_drain.drain.repository.DrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DrainService {

    private final DrainRepository drainRepository;

    // TODO: 빗물받이 등록/조회/상태 변경 로직 구현 예정
}
