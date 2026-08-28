package com.hanium.smart_drain.global.security.jwt;

import com.hanium.smart_drain.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
        @Value("${jwt.secret:change-this-secret-change-this-secret-change-this}") String secret,
        @Value("${jwt.access-token-expiration-ms:3600000}") long accessTokenExpirationMs,
        @Value("${jwt.refresh-token-expiration-ms:1209600000}") long refreshTokenExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
            .subject(String.valueOf(user.getUserId()))
            .claim("username", user.getUsername())
            .claim("role", user.getRole().name())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
            .subject(String.valueOf(user.getUserId()))
            .claim("tokenType", "refresh")
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }

        String role = claims.get("role", String.class);
        if (role == null || !(role.equals("ROLE_ADMIN") || role.equals("ROLE_WORKER"))) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(
            claims.getSubject(),
            null,
            List.of(new SimpleGrantedAuthority(role))
        );
    }

    public LocalDateTime calculateRefreshTokenExpiresAt() {
        return LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000L);
    }

    public LocalDateTime now() {
        return LocalDateTime.now(ZoneId.systemDefault());
    }
}
