package com.hanium.smart_drain.auth.dto;

import com.hanium.smart_drain.auth.entity.UserRole;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {

    private Long userId;
    private String username;
    private String name;
    private UserRole role;
    private LocalDateTime registeredAt;
}
