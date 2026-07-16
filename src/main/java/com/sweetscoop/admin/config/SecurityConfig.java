package com.sweetscoop.admin.config;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 보안 비활성화
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. CORS 설정을 Security 필터에 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 3. 요청 권한 설정 (순서 정밀 조정 완료)
            .authorizeHttpRequests(auth -> auth
                // Cors PreFlight 요청은 항상 무조건 통과시킵니다.
                .requestMatchers(request -> CorsUtils.isPreFlightRequest(request)).permitAll()
                
                // 💡 /api/admin/** 하위의 모든 리소스 요청을 인증 없이 패스시킵니다.
                .requestMatchers("/api/admin/**").permitAll() 
                
                // 💡 그 외의 다른 모든 요청들도 일단 모두 허용(permitAll)하여 차단벽을 완전히 없앱니다.
                // (.anyRequest()는 무조건 체인의 맨 마지막에 한 번만 단독 배치되어야 에러가 나지 않습니다!)
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000")); 
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}