package com.sweetscoop.admin.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sweetscoop.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/inventory/requests")
@RequiredArgsConstructor
public class InventoryRequestController {

    private final InventoryService inventoryService;

    /**
     * 배송 상태 변경 (준비중 -> 배송중 -> 배송완료)
     * PATCH /api/admin/inventory/requests/{id}/delivery
     */
    @PatchMapping("/{id}/delivery")
    public ResponseEntity<String> updateDeliveryStatus(
            @PathVariable("id") Integer requestId,
            @RequestBody Map<String, String> requestBody) {

        String deliveryStatus = requestBody.get("deliveryStatus");

        if (deliveryStatus == null || deliveryStatus.isBlank()) {
            return ResponseEntity.badRequest().body("배송 상태 값이 없습니다.");
        }

        // 서비스 호출: 배송 상태 변경 및 '배송완료'시 지점 재고 충전 로직 실행
        inventoryService.updateDeliveryStatus(requestId, deliveryStatus);

        return ResponseEntity.ok("배송 상태가 성공적으로 변경되었습니다.");
    }
}