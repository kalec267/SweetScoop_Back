package com.sweetscoop.admin.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/admin/sse")
@CrossOrigin(origins = "http://localhost:5173")
public class SseController {

    // 연결된 관리자 브라우저 세션들을 저장하는 맵
    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 1. 프론트엔드 연결 엔드포인트
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam String adminId) {
        SseEmitter emitter = new SseEmitter(60 * 1000L * 30); // 30분 만료 설정
        emitters.put(adminId, emitter);

        emitter.onCompletion(() -> emitters.remove(adminId));
        emitter.onTimeout(() -> emitters.remove(adminId));

        try {
            // 최초 연결 성공 메시지 발송
            emitter.send(SseEmitter.event().name("connect").data("실시간 알림 연결 완료"));
        } catch (IOException e) {
            emitters.remove(adminId);
        }
        return emitter;
    }

    // 2. 외부(지점앱 등)에서 알림을 유발할 때 호출하는 메서드 (이벤트 브로드캐스팅)
    public static void sendNotification(String message) {
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("notification").data(message));
            } catch (IOException e) {
                emitters.remove(id);
            }
        });
    }
}