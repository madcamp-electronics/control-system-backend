package com.hanium.smart_drain.worker.service;

import com.hanium.smart_drain.auth.entity.UserRole;
import com.hanium.smart_drain.auth.repository.UserRepository;
import com.hanium.smart_drain.worker.dto.WorkerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WorkerResponse> getWorkers() {
        return userRepository.findByRoleOrderByUserIdAsc(UserRole.ROLE_WORKER)
            .stream()
            .map(user -> WorkerResponse.builder()
                .workerId(user.getUserId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .build())
            .toList();
    }
}
