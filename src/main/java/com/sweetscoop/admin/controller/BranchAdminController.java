package com.sweetscoop.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.sweetscoop.admin.entity.Item;
import com.sweetscoop.admin.repository.BranchInventoryRepository;
import com.sweetscoop.admin.repository.HqInventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173","http://192.168.137.173:5173"})
public class BranchAdminController {

    private final BranchInventoryRepository branchInventoryRepository;
    private final HqInventoryRepository hqInventoryRepository;

    // 분점 영업 상태 변경
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateBranchStatus(
            @PathVariable Integer id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                "분점 영업 상태가 [" + status + "](으)로 변경되었습니다."
        );
    }

    // 특정 지점 실시간 원자재 재고 조회
    @GetMapping("/{branchId}/inventory")
    public ResponseEntity<List<BranchInventory>> getBranchInventory(
            @PathVariable Integer branchId
    ) {
        log.info(
                "[Branch API] 지점 재고 조회 요청 - 지점 ID: {}",
                branchId
        );

        List<BranchInventory> inventory =
                branchInventoryRepository.findByBranchIdWithItem(branchId);

        log.info(
                "[Branch API] 지점 재고 조회 완료 - {}건",
                inventory.size()
        );

        return ResponseEntity.ok(inventory);
    }

    // 본사 재고 발주 신청
    @PostMapping("/orders")
    public ResponseEntity<String> createOrderRequest(
            @RequestBody Map<String, Object> payload
    ) {
        log.info(
                "[Branch API] 본사 발주 신청 요청 - Payload: {}",
                payload
        );

        try {
            Integer branchId =
                    Integer.parseInt(payload.get("branchId").toString());

            Integer itemId =
                    Integer.parseInt(payload.get("itemId").toString());

            Integer quantity =
                    Integer.parseInt(payload.get("quantity").toString());

            if (quantity <= 0) {
                return ResponseEntity.badRequest()
                        .body("발주 수량은 1개 이상이어야 합니다.");
            }

            List<BranchInventory> inventory =
                    branchInventoryRepository
                            .findByBranchIdWithItem(branchId);

            if (inventory.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("해당 지점의 재고 정보가 없습니다.");
            }

            com.sweetscoop.branch.entity.Branch targetBranch =
                    inventory.get(0).getBranch();

            Item targetItem = inventory.stream()
                    .map(BranchInventory::getItem)
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);

            if (targetItem == null) {
                return ResponseEntity.badRequest()
                        .body("등록되지 않은 원자재 아이템입니다.");
            }

            HqInventory hqOrder = HqInventory.builder()
                    .branch(targetBranch)
                    .item(targetItem)
                    .requestQuantity(quantity)
                    .approvalStatus("대기중")
                    .deliveryStatus("준비중")
                    .build();

            hqInventoryRepository.save(hqOrder);

            log.info(
                    "[Branch API] 발주 저장 완료 - 발주 ID: {}",
                    hqOrder.getId()
            );

            SseController.sendNotification(
                    targetBranch.getBranchName()
                            + "에서 새로운 재고 신청이 등록되었습니다."
            );

            return ResponseEntity.ok(
                    "발주 신청이 성공적으로 등록되었습니다."
            );

        } catch (NullPointerException e) {
            log.error("필수 발주 데이터 누락", e);

            return ResponseEntity.badRequest()
                    .body("branchId, itemId, quantity는 필수입니다.");

        } catch (NumberFormatException e) {
            log.error("발주 데이터 형식 오류", e);

            return ResponseEntity.badRequest()
                    .body("branchId, itemId, quantity는 숫자여야 합니다.");

        } catch (Exception e) {
            log.error("발주 저장 중 오류 발생", e);

            return ResponseEntity.internalServerError()
                    .body("서버 내부 저장 오류: " + e.getMessage());
        }
    }

    // 특정 지점 발주 내역 조회
    @GetMapping("/{branchId}/orders")
    public ResponseEntity<List<HqInventory>> getBranchOrders(
            @PathVariable Integer branchId
    ) {
        log.info(
                "[Branch API] 지점 발주 내역 조회 - 지점 ID: {}",
                branchId
        );

        List<HqInventory> orders =
                hqInventoryRepository.findByBranch_Id(branchId);

        return ResponseEntity.ok(orders);
    }
}