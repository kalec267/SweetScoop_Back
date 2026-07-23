package com.sweetscoop.admin.repository;

import com.sweetscoop.admin.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 권한(HQ/BRANCH)별 최신 20개 알림 조회
    List<Notification> findTop20ByTarget(String targetRole);
}