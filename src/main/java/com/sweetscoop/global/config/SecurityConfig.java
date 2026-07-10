package com.sweetscoop.global.config; // 대철님의 실제 패키지 경로에 맞게 지정하세요

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Postman 테스트를 위해 CSRF 보안을 완전히 끕니다.
            .csrf(csrf -> csrf.disable())
            
            // 2. 기본 로그인 폼과 HTTP Basic 인증창을 모두 비활성화합니다.
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // 3. /inventory 및 /sales를 포함한 모든 URL 요청을 무조건 허용합니다.
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }
}