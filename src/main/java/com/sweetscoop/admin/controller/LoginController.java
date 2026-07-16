package com.sweetscoop.admin.controller;

import com.sweetscoop.admin.service.AuthService;
import com.sweetscoop.admin.service.AuthService.LoginRequest;
import com.sweetscoop.admin.service.AuthService.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class LoginController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("[Login Controller] 로그인 시도 수신 - Role: {}, ID: {}", request.getRole(), request.getUsername());

        LoginResponse response = authService.authenticate(request);

        if (response.isSuccess()) {
            log.info("[Login Controller] 인증 성공 -> 닉네임: {}", response.getName());
            return ResponseEntity.ok(response);
        } else {
            log.warn("[Login Controller] 인증 실패 -> 자격 증명이 올바르지 않음 (ID: {})", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }
}