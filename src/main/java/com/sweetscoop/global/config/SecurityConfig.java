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

    /**
     * Spring Security 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            /*
             * Vue와 Spring Boot가 서로 다른 주소 또는 포트에서 실행되므로
             * REST API 개발 단계에서는 CSRF를 비활성화한다.
             */
            .csrf(AbstractHttpConfigurer::disable)

            /*
             * 아래에서 정의한 CORS 정책을 Spring Security에 적용한다.
             */
            .cors(cors -> cors.configurationSource(
                corsConfigurationSource()
            ))

            /*
             * Spring Security 기본 로그인 화면을 사용하지 않는다.
             */
            .formLogin(AbstractHttpConfigurer::disable)

            /*
             * 브라우저의 HTTP Basic 인증 팝업을 비활성화한다.
             */
            .httpBasic(AbstractHttpConfigurer::disable)

            /*
             * URL별 접근 권한을 설정한다.
             */
            .authorizeHttpRequests(auth -> auth

                /*
                 * OPTIONS 방식의 CORS 사전 요청은 인증 없이 허용한다.
                 */
                .requestMatchers(CorsUtils::isPreFlightRequest)
                .permitAll()

                /*
                 * Vue 빌드 결과물과 정적 리소스를 허용한다.
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
                 * 관리자 API를 허용한다.
                 */
                .requestMatchers("/api/admin/**")
                .permitAll()

                /*
                 * 일반 API를 허용한다.
                 */
                .requestMatchers(
                    "/api/**",
                    "/inventory/**",
                    "/sales/**"
                )
                .permitAll()

                /*
                 * 현재 개발 단계에서는 나머지 요청도 허용한다.
                 *
                 * 이후 JWT 또는 세션 인증을 적용할 경우
                 * authenticated()로 변경할 수 있다.
                 */
                .anyRequest()
                .permitAll()
            );

        return http.build();
    }

    /**
     * CORS 정책 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        /*
         * Vue 개발 서버에서 요청할 수 있는 Origin 목록
         *
         * Origin은 프로토콜 + IP 또는 도메인 + 포트가
         * 모두 일치해야 한다.
         */
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:5300",
            "http://192.168.137.173:5173",
            "http://192.168.137.173:5300",
            "http://172.30.1.17:5300"
        ));

        /*
         * 허용할 HTTP 메서드
         */
        configuration.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        ));

        /*
         * 요청 헤더 전체 허용
         */
        configuration.setAllowedHeaders(List.of("*"));

        /*
         * 프론트엔드에서 접근할 수 있는 응답 헤더
         *
         * JWT를 Authorization 응답 헤더로 전달하는 경우 필요하다.
         */
        configuration.setExposedHeaders(List.of(
            "Authorization",
            "Content-Disposition"
        ));

        /*
         * 쿠키, 세션, Authorization 헤더와 같은
         * 인증 정보를 포함한 요청을 허용한다.
         */
        configuration.setAllowCredentials(true);

        /*
         * 브라우저가 OPTIONS 사전 요청 결과를
         * 1시간 동안 캐시한다.
         */
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        /*
         * 모든 Spring Boot URL에 CORS 설정 적용
         */
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}