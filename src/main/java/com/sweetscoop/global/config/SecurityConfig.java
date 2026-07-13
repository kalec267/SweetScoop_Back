package com.sweetscoop.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 테스트 편의를 위해 CSRF 보안 및 기본 로그인 폼 비활성화
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            // 모든 /inventory 및 /sales 하위 API 요청을 무조건 허용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/inventory/**", "/sales/**","/api/admin/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Vue.js 서버 주소 허용
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        // 모든 HTTP 메서드 허용 (GET, POST 등)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 모든 HTTP 헤더 허용
        configuration.setAllowedHeaders(List.of("*"));
        // 쿠키 및 인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 백엔드의 모든 API 경로(/**)에 위의 정책을 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
