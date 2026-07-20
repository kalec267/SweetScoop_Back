package com.sweetscoop.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Spring Security 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
            /*
             * Vue와 Spring Boot가 서로 다른 Origin에서 실행되므로
             * REST API 개발 단계에서는 CSRF를 비활성화한다.
             */
            .csrf(AbstractHttpConfigurer::disable)

            /*
             * 아래에서 정의한 CORS 설정을
             * Spring Security 필터에 적용한다.
             */
            .cors(cors ->
                cors.configurationSource(
                    corsConfigurationSource
                )
            )

            /*
             * Spring Security 기본 로그인 화면 비활성화
             */
            .formLogin(
                AbstractHttpConfigurer::disable
            )

            /*
             * HTTP Basic 인증 팝업 비활성화
             */
            .httpBasic(
                AbstractHttpConfigurer::disable
            )

            /*
             * URL별 접근 권한 설정
             */
            .authorizeHttpRequests(auth -> auth

                /*
                 * 브라우저의 OPTIONS 사전 요청 허용
                 */
                .requestMatchers(
                    CorsUtils::isPreFlightRequest
                )
                .permitAll()

                /*
                 * Vue 정적 리소스 허용
                 */
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/assets/**",
                    "/images/**",
                    "/favicon.svg",
                    "/icons.svg",
                    "/error"
                )
                .permitAll()

                /*
                 * 개발 단계에서는 모든 API 허용
                 */
                .requestMatchers(
                    "/api/**",
                    "/inventory/**",
                    "/sales/**"
                )
                .permitAll()

                /*
                 * 현재 개발 단계에서는 나머지도 허용
                 *
                 * JWT 또는 세션 인증을 적용할 때는
                 * authenticated() 등으로 변경한다.
                 */
                .anyRequest()
                .permitAll()
            );

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource
            corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        /*
         * Vue 개발 서버 허용 주소
         *
         * Origin은 프로토콜, IP 또는 도메인,
         * 포트가 모두 일치해야 한다.
         */
        configuration.setAllowedOrigins(
            List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5300",

                "http://192.168.137.173:5173",
                "http://192.168.137.173:5300",

                "http://172.30.1.17:5173",
                "http://172.30.1.17:5300"
            )
        );

        /*
         * 허용 HTTP 메서드
         */
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

        /*
         * 모든 요청 헤더 허용
         */
        configuration.setAllowedHeaders(
            List.of("*")
        );

        /*
         * Vue에서 읽을 수 있도록 노출할 응답 헤더
         */
        configuration.setExposedHeaders(
            List.of(
                "Authorization",
                "Content-Disposition"
            )
        );

        /*
         * 쿠키, 세션, Authorization 헤더 허용
         */
        configuration.setAllowCredentials(true);

        /*
         * OPTIONS 사전 요청 결과를
         * 브라우저에서 1시간 동안 캐시
         */
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        /*
         * 모든 Spring Boot 요청 경로에 적용
         */
        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }

    /**
     * Toss Payments API 호출 등에 사용하는 RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
