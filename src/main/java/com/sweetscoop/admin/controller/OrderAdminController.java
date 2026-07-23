package com.sweetscoop.admin.controller;

import com.sweetscoop.order.service.OrderService; // OrderService 임포트
import com.sweetscoop.scheduler.OrderResetScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // @DeleteMapping 추가

@RestController
@RequestMapping("/api/admin/orders")
public class OrderAdminController {

    @Autowired
    private OrderResetScheduler orderResetScheduler;

    @Autowired
    private OrderService orderService; // 주문 서비스 주입

    // 버튼 클릭으로 즉시 초기화를 테스트하는 API
    @PostMapping("/reset")
    public ResponseEntity<String> manualResetOrders() {
        try {
            orderResetScheduler.resetDailyOrders();
            return ResponseEntity.ok("주문 환경이 성공적으로 초기화되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("초기화 실패: " + e.getMessage());
        }
    }

    /**
     * 주문 취소 및 DB 삭제 API
     * DELETE /api/admin/orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") int id) {
        try {
            orderService.deleteOrder(id); // Service -> DAO -> MyBatis로 ORDERS 테이블 삭제 수행
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}