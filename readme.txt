260723

본사 대시보드 수정을 위한 전체적인 Controller,service 수정
[AdminCouponController]-미연결 템플릿 쿠폰 목록만 조회
@GetMapping("/templates")
    public ResponseEntity<List<CouponDto>> getCouponTemplates() {
        return ResponseEntity.ok(couponService.getCouponTemplates());
}

[CouponService]-템플릿 쿠폰 목록 조회 메서드
public List<CouponDto> getCouponTemplates() {
        return couponRepository.findAll()
                .stream()
                .filter(coupon -> coupon.getMemberId() == null) // 템플릿 쿠폰만 필터링
                .sorted((a, b) -> b.getId().compareTo(a.getId())) // 최신순 정렬
                .map(CouponDto::new)
                .toList();
}

[Notification 로직 추가 ]-프론트의 헤더부분의 알람에 추가될 테이블 생성 및 로직 추가

-repository-
package com.sweetscoop.admin.repository;

import com.sweetscoop.admin.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 권한(HQ/BRANCH)별 최신 20개 알림 조회
    List<Notification> findTop20ByTarget(String targetRole);
}