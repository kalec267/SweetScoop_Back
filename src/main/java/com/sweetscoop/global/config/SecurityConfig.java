package com.sweetscoop.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            // REST API 방식이므로 CSRF 비활성화
            .csrf(AbstractHttpConfigurer::disable)

            // CORS 설정 적용
            .cors(cors -> cors.configurationSource(
                corsConfigurationSource()
            ))

            // 기본 로그인 화면 비활성화
            .formLogin(AbstractHttpConfigurer::disable)

            // HTTP Basic 인증 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)

            // 요청별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth

                // CORS 사전 요청 허용
                .requestMatchers(request ->
                    CorsUtils.isPreFlightRequest(request)
                ).permitAll()

                // Vue 정적 파일 허용
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/assets/**",
                    "/images/**",
                    "/favicon.svg",
                    "/icons.svg",
                    "/error"
                ).permitAll()

                // 관리자 API 허용
                .requestMatchers(
                    "/api/admin/**"
                ).permitAll()

                // 일반 백엔드 API 허용
                .requestMatchers(
                    "/api/**",
                    "/inventory/**",
                    "/sales/**"
                ).permitAll()

                // 개발 단계에서는 나머지 요청도 허용
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
            new CorsConfiguration();

        // Vue 개발 서버 주소
        configuration.setAllowedOrigins(
            List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://192.168.137.173:5173",
                "http://192.168.137.173:5300",
                "http://localhost:5300",
                "http://172.30.1.17:5300"
            )
        );

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of("*")
        );

        configuration.setAllowCredentials(true);

        // 브라우저가 Preflight 결과를 1시간 저장
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }
}