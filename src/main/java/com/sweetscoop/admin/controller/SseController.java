package com.sweetscoop.admin.controller;

import com.sweetscoop.admin.entity.Notification;
import com.sweetscoop.admin.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    // 1. 실시간 SSE 연결 구독 (Role 기반 분기)
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(
            @RequestParam("role") String role, 
            @RequestParam("adminId") String adminId) {
        return sseService.subscribe(role, adminId);
    }

    // 2. 새로고침 시 기존 알림 목록 조회
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam("role") String role) {
        return ResponseEntity.ok(sseService.getNotifications(role));
    }
}