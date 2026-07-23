package com.sweetscoop.admin.service;

import com.sweetscoop.admin.entity.Notification;
import com.sweetscoop.admin.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private final Map<String, Map<String, SseEmitter>> roleEmitters = new ConcurrentHashMap<>();
    private final NotificationRepository notificationRepository;

    // 1. SSE 연결 구독
    public SseEmitter subscribe(String role, String clientId) {
        SseEmitter emitter = new SseEmitter(60 * 1000L * 30); // 30분 만료

        roleEmitters.computeIfAbsent(role, k -> new ConcurrentHashMap<>()).put(clientId, emitter);

        emitter.onCompletion(() -> removeEmitter(role, clientId));
        emitter.onTimeout(() -> removeEmitter(role, clientId));
        emitter.onError((e) -> removeEmitter(role, clientId)); // 💡 에러 발생 시 자동 제거 추가

        try {
            emitter.send(SseEmitter.event().name("connect").data("실시간 알림 연결 완료"));
        } catch (Exception e) {
            removeEmitter(role, clientId);
        }

        return emitter;
    }

    // 2. 특정 Role에 알림 전송 (💡 IOException 예외 안전 처리)
    public void sendNotificationToRole(String targetRole, String message) {
        // DB 저장 (새로고침 시 유지용)
        Notification notification = new Notification();
        notification.setTarget(targetRole);
        notification.setMessage(message);
        notificationRepository.save(notification);

        Map<String, SseEmitter> emitters = roleEmitters.get(targetRole);
        if (emitters != null) {
            emitters.forEach((clientId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event().name("notification").data(message));
                } catch (Exception e) {
                    // 💡 연결 끊긴 Client Emitter 로그 출력 후 즉시 안전하게 제거
                    log.warn("[SSE] 끊어진 클라이언트 연결 제거 - Role: {}, ClientId: {}", targetRole, clientId);
                    removeEmitter(targetRole, clientId);
                }
            });
        }
    }

    public List<Notification> getNotifications(String role) {
        return notificationRepository.findTop20ByTarget(role);
    }

    private void removeEmitter(String role, String clientId) {
        Map<String, SseEmitter> emitters = roleEmitters.get(role);
        if (emitters != null) {
            emitters.remove(clientId);
        }
    }
}