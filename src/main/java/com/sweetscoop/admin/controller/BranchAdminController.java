package com.sweetscoop.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.admin.entity.BranchInventory;
import com.sweetscoop.admin.entity.HqInventory;
import com.sweetscoop.admin.repository.BranchInventoryRepository;
import com.sweetscoop.admin.repository.HqInventoryRepository;
import com.sweetscoop.admin.service.BranchAdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor
@Slf4j
public class BranchAdminController {

    private final BranchAdminService branchAdminService; // 💡 서비스 계층만 주입받아 위임
    private final BranchInventoryRepository branchInventoryRepository;
    private final HqInventoryRepository hqInventoryRepository;

    // 1. 분점 영업 상태 변경
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateBranchStatus(
            @PathVariable Integer id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok("분점 영업 상태가 [" + status + "](으)로 변경되었습니다.");
    }

    // 2. 특정 지점 실시간 원자재 재고 조회
    @GetMapping("/{branchId}/inventory")
    public ResponseEntity<List<BranchInventory>> getBranchInventory(
            @PathVariable Integer branchId
    ) {
        log.info("[Branch API] 지점 재고 조회 요청 - 지점 ID: {}", branchId);
        List<BranchInventory> inventory = branchInventoryRepository.findByBranchIdWithItem(branchId);
        return ResponseEntity.ok(inventory);
    }

    // 3. 본사 재고 발주 신청 (💡 컨트롤러는 요청 수신 및 서비스 위임만 담당)
    @PostMapping("/orders")
    public ResponseEntity<String> createOrderRequest(@RequestBody Map<String, Object> payload) {
        log.info("[Branch API] 본사 발주 신청 요청 - Payload: {}", payload);

        try {
            Integer branchId = Integer.parseInt(payload.get("branchId").toString());
            Integer itemId = Integer.parseInt(payload.get("itemId").toString());
            Integer quantity = Integer.parseInt(payload.get("quantity").toString());

            // 🚀 비즈니스 로직(DB 저장 + SSE 알림)은 Service로 통째로 전달
            branchAdminService.requestInventory(branchId, itemId, quantity);

            return ResponseEntity.ok("발주 신청이 성공적으로 등록되었습니다.");

        } catch (NullPointerException e) {
            log.error("필수 발주 데이터 누락", e);
            return ResponseEntity.badRequest().body("branchId, itemId, quantity는 필수입니다.");

        } catch (NumberFormatException e) {
            log.error("발주 데이터 형식 오류", e);
            return ResponseEntity.badRequest().body("branchId, itemId, quantity는 숫자여야 합니다.");

        } catch (IllegalArgumentException e) {
            log.warn("발주 신청 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("발주 저장 중 서버 내부 오류 발생", e);
            return ResponseEntity.internalServerError().body("서버 내부 저장 오류: " + e.getMessage());
        }
    }
    //4. 전체 지점 발주 내역 조회 (대시보드 및 본사 발주 관리용)
    @GetMapping("/orders")
    public ResponseEntity<List<HqInventory>> getAllOrders() {
        log.info("[Branch API] 전체 지점 발주 내역 조회 요청");
        List<HqInventory> orders = hqInventoryRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    // 5. 특정 지점 발주 내역 조회
    @GetMapping("/{branchId}/orders")
    public ResponseEntity<List<HqInventory>> getBranchOrders(
            @PathVariable Integer branchId
    ) {
        log.info("[Branch API] 지점 발주 내역 조회 - 지점 ID: {}", branchId);
        List<HqInventory> orders = hqInventoryRepository.findByBranch_Id(branchId);
        return ResponseEntity.ok(orders);
    }
}