package com.sweetscoop.admin.controller;

import com.sweetscoop.admin.dto.response.DashboardSummaryResponse;
import com.sweetscoop.admin.dto.response.InventoryRequestListResponse;
import com.sweetscoop.admin.entity.HqInventory;
import com.sweetscoop.branch.repository.BranchRepository;
import com.sweetscoop.admin.repository.HqInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final HqInventoryRepository hqInventoryRepository;
    private final BranchRepository branchRepository;

    // 1. 상단 4단 카드 요약 데이터 API
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        long totalRequests = hqInventoryRepository.count(); // 전체 건수
        long pendingCount = hqInventoryRepository.countByApprovalStatus("대기 중"); // 대기 건수
        long shippingCount = hqInventoryRepository.countByDeliveryStatus("배송 중"); // 배송중 건수
        long activeBranches = branchRepository.count(); // 전체 분점 수

        DashboardSummaryResponse response = DashboardSummaryResponse.builder()
                .totalRequests((int) totalRequests)
                .pendingCount((int) pendingCount)
                .shippingCount((int) shippingCount)
                .activeBranches((int) activeBranches)
                .build();

        return ResponseEntity.ok(response);
    }

    // 2. 하단 재고 신청 현황 리스트 API
    @GetMapping("/inventory/requests")
    public ResponseEntity<List<InventoryRequestListResponse>> getInventoryRequests() {
        // N+1 문제를 방지하기 위해 이전에 만든 fetch join 쿼리 사용
        List<HqInventory> hqInventories = hqInventoryRepository.findAllWithBranchAndItem();

        List<InventoryRequestListResponse> response = hqInventories.stream()
                .map(h -> InventoryRequestListResponse.builder()
                        .requestId(h.getId())
                        .branchName(h.getBranch().getBranchName())
                        .requestMenu(h.getItem().getItemName()) // 물류 실물품명 매핑
                        .quantity(h.getRequestQuantity())
                        .status(h.getApprovalStatus()) // '대기 중', '승인완료', '반려' 등
                        // 필요 시 배송 상태와 조합하여 '배송 중' 스위칭 가능
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
