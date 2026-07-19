package com.sweetscoop.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.admin.dto.request.RegisterSaveDto;
import com.sweetscoop.admin.service.RegisterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterSaveDto request ) {

        registerService.register(request);

        return ResponseEntity.ok("회원가입 성공");
    }

    
    
}