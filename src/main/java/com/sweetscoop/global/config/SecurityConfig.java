//package com.sweetscoop.global.config;
//
//import java.util.List;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(
//            HttpSecurity http) throws Exception {
//
//        http
//            .cors(cors -> cors.configurationSource(
//                corsConfigurationSource()
//            ))
//            .csrf(csrf -> csrf.disable())
//            .formLogin(form -> form.disable())
//            .httpBasic(basic -> basic.disable())
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(
//                    "/api/**",
//                    "/inventory/**",
//                    "/sales/**"
//                ).permitAll()
//                .anyRequest().permitAll()
//            );
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//
//        CorsConfiguration configuration =
//            new CorsConfiguration();
//
//        configuration.setAllowedOrigins(
//            List.of(
//                "http://localhost:5173",
//                "http://localhost:5174"
//            )
//        );
//
//        configuration.setAllowedMethods(
//            List.of(
//                "GET",
//                "POST",
//                "PUT",
//                "PATCH",
//                "DELETE",
//                "OPTIONS"
//            )
//        );
//
//        configuration.setAllowedHeaders(
//            List.of("*")
//        );
//
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source =
//            new UrlBasedCorsConfigurationSource();
//
//        source.registerCorsConfiguration(
//            "/**",
//            configuration
//        );
//
//        return source;
//    }
//}


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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(
                    corsConfigurationSource()
            ))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .authorizeHttpRequests(auth -> auth

                // Vue 정적 파일
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/assets/**",
                    "/images/**",
                    "/favicon.svg",
                    "/icons.svg",
                    "/error"
                ).permitAll()

                // 백엔드 API
                .requestMatchers(
                    "/api/**",
                    "/inventory/**",
                    "/sales/**"
                ).permitAll()

                // 개발 단계에서는 모두 허용
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOriginPatterns(
                List.of("*")
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

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}