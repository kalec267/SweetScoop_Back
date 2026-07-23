package com.sweetscoop.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sweetscoop.admin.entity.BranchInventory;
import com.sweetscoop.admin.entity.HqInventory;
import com.sweetscoop.admin.repository.BranchInventoryRepository;
import com.sweetscoop.admin.repository.HqInventoryRepository;
import com.sweetscoop.item.entity.Item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchAdminService {

    private final BranchInventoryRepository branchInventoryRepository;
    private final HqInventoryRepository hqInventoryRepository;
    private final SseService sseService;

    /**
     * 분점 재고 발주 신청 DB 저장 처리 및 본사("HQ") 대상 실시간 SSE 알림 전송
     */
    @Transactional
    public void requestInventory(Integer branchId, Integer itemId, Integer quantity) {
        // 0. 수량 검증
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("발주 수량은 1개 이상이어야 합니다.");
        }

        // 1. 지점 재고 및 지점(Branch) 정보 DB 조회
        List<BranchInventory> inventory = branchInventoryRepository.findByBranchIdWithItem(branchId);
        if (inventory.isEmpty()) {
            throw new IllegalArgumentException("해당 지점의 재고 정보가 존재하지 않습니다. branchId=" + branchId);
        }

        com.sweetscoop.branch.entity.Branch targetBranch = inventory.get(0).getBranch();

        // 2. 발주 대상 원자재 아이템(Item) 매핑
        Item targetItem = inventory.stream()
                .map(BranchInventory::getItem)
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 원자재 아이템입니다. itemId=" + itemId));

        // 3. 본사 발주 데이터 생성 및 DB 저장 (HQ_INVENTORY INSERT)
        HqInventory hqOrder = HqInventory.builder()
                .branch(targetBranch)
                .item(targetItem)
                .requestQuantity(quantity)
                .approvalStatus("대기중")
                .deliveryStatus("준비중")
                .build();

        hqInventoryRepository.save(hqOrder);
        log.info("[Branch Service] 발주 DB 저장 완료 - 발주 ID: {}, 지점: {}, 상품: {}, 수량: {}", 
                hqOrder.getId(), targetBranch.getBranchName(), targetItem.getItemName(), quantity);

        // 4. 본사("HQ") 관리자 대상 실시간 SSE 알림 발송 + NOTIFICATION DB 저장
        String notiMessage = String.format("[%s]에서 %s %d개 재고 신청이 등록되었습니다.", 
                targetBranch.getBranchName(), targetItem.getItemName(), quantity);
        
        sseService.sendNotificationToRole("HQ", notiMessage);
    }
}