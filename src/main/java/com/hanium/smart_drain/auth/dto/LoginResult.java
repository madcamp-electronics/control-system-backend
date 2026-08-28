package com.hanium.smart_drain.auth.dto;

import com.hanium.smart_drain.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {

    private Long userId;
    private String accessToken;
    private String refreshToken;
    private String username;
    private String name;
    private UserRole role;
}
