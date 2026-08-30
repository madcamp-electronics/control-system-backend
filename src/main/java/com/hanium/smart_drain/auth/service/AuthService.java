package com.hanium.smart_drain.auth.service;

import com.hanium.smart_drain.auth.dto.LoginRequest;
import com.hanium.smart_drain.auth.dto.LoginResult;
import com.hanium.smart_drain.auth.entity.RefreshToken;
import com.hanium.smart_drain.auth.dto.SignupRequest;
import com.hanium.smart_drain.auth.dto.SignupResponse;
import com.hanium.smart_drain.auth.entity.User;
import com.hanium.smart_drain.auth.repository.RefreshTokenRepository;
import com.hanium.smart_drain.auth.repository.UserRepository;
import com.hanium.smart_drain.global.exception.CustomException;
import com.hanium.smart_drain.global.exception.ErrorCode;
import com.hanium.smart_drain.global.security.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "username already exists");
        }

        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .phoneNumber(request.getPhoneNumber())
            .role(request.getRole())
            .registeredAt(LocalDateTime.now())
            .build();

        User savedUser = userRepository.save(user);

        return SignupResponse.builder()
            .userId(savedUser.getUserId())
            .username(savedUser.getUsername())
            .name(savedUser.getName())
            .role(savedUser.getRole())
            .registeredAt(savedUser.getRegisteredAt())
            .build();
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "invalid credentials");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user);
        LocalDateTime now = jwtTokenProvider.now();

        refreshTokenRepository.revokeAllActiveTokensByUserId(user.getUserId(), now);
        refreshTokenRepository.save(
            RefreshToken.builder()
                .userId(user.getUserId())
                .token(refreshTokenValue)
                .expiresAt(jwtTokenProvider.calculateRefreshTokenExpiresAt())
                .issuedAt(now)
                .revoked(false)
                .revokedAt(null)
                .build()
        );

        return LoginResult.builder()
            .userId(user.getUserId())
            .accessToken(accessToken)
            .refreshToken(refreshTokenValue)
            .username(user.getUsername())
            .name(user.getName())
            .role(user.getRole())
            .build();
    }
}
