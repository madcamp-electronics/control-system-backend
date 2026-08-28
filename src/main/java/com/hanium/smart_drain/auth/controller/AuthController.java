package com.hanium.smart_drain.auth.controller;

import com.hanium.smart_drain.auth.dto.LoginRequest;
import com.hanium.smart_drain.auth.dto.LoginResult;
import com.hanium.smart_drain.auth.dto.LoginResponse;
import com.hanium.smart_drain.auth.dto.SignupRequest;
import com.hanium.smart_drain.auth.dto.SignupResponse;
import com.hanium.smart_drain.auth.service.AuthService;
import com.hanium.smart_drain.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @Value("${jwt.refresh-token-expiration-ms:1209600000}")
    private long refreshTokenExpirationMs;
    @Value("${auth.refresh-cookie-name:refreshToken}")
    private String refreshCookieName;
    @Value("${auth.refresh-cookie-secure:false}")
    private boolean refreshCookieSecure;
    @Value("${auth.refresh-cookie-same-site:Lax}")
    private String refreshCookieSameSite;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResult loginResult = authService.login(request);

        ResponseCookie refreshCookie = ResponseCookie.from(refreshCookieName, loginResult.getRefreshToken())
            .httpOnly(true)
            .secure(refreshCookieSecure)
            .path("/")
            .maxAge(refreshTokenExpirationMs / 1000)
            .sameSite(refreshCookieSameSite)
            .build();

        LoginResponse response = LoginResponse.builder()
            .userId(loginResult.getUserId())
            .accessToken(loginResult.getAccessToken())
            .username(loginResult.getUsername())
            .name(loginResult.getName())
            .role(loginResult.getRole())
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(ApiResponse.success(response));
    }
}
