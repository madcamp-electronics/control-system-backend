package com.hanium.smart_drain.global.security;

import com.hanium.smart_drain.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/drains").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/drains/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/drains", "/api/v1/drains/**")
                    .hasAnyRole("ADMIN", "WORKER")
                .requestMatchers(HttpMethod.GET, "/api/v1/alerts", "/api/v1/alerts/**")
                    .hasAnyRole("ADMIN", "WORKER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/alerts/*/status").hasRole("WORKER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/alerts/*/assignment").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/alerts/**").hasRole("WORKER")
                .requestMatchers(HttpMethod.GET, "/api/v1/workers").hasRole("ADMIN")
                .requestMatchers("/api/v1/**").permitAll()
                .requestMatchers("/storage/**", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
