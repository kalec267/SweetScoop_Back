package com.sweetscoop.admin.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sweetscoop.admin.entity.BranchInventory;
import com.sweetscoop.admin.entity.HqInventory;
import com.sweetscoop.admin.repository.BranchInventoryRepository;
import com.sweetscoop.admin.repository.HqInventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final HqInventoryRepository hqInventoryRepository;
    private final BranchInventoryRepository branchInventoryRepository;

    /**
     * 1. 전체 발주/배송 목록 조회
     */
    @Transactional(readOnly = true)
    public List<HqInventory> getAllDeliveryOrders() {
        return hqInventoryRepository.findAllWithBranchAndItem();
    }

    /**
     * 2. 본사 발주 승인 로직 (HqOrderManagement.vue 연동)
     * 승인 시 approvalStatus -> '승인완료', deliveryStatus -> '준비중'
     */
    @Transactional
    public void approveOrder(Integer hqInventoryId) {
        HqInventory hqInventory = hqInventoryRepository.findById(hqInventoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 발주 건입니다. ID: " + hqInventoryId));

        hqInventory.setApprovalStatus("승인완료");
        hqInventory.setDeliveryStatus("준비중"); // 💡 승인 즉시 배송 준비 상태로 전환!

        hqInventoryRepository.save(hqInventory);
        log.info("[Delivery Service] 발주 승인 완료 - ID: {}, 배송상태: 준비중", hqInventoryId);
    }

    /**
     * 3. 배송 상태 변경 및 배송 완료 시 지점 재고 자동 반영 (Delivery.vue 연동)
     */
    @Transactional
    public void updateDeliveryStatus(Integer orderId, String deliveryStatus) {
        HqInventory hqInventory = hqInventoryRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 발주 건입니다. ID: " + orderId));

        // 배송 상태 업데이트 (준비중 -> 배송중 -> 배송완료)
        hqInventory.setDeliveryStatus(deliveryStatus);

        // 💡 배송 완료 시 해당 지점의 재고(BranchInventory) 수량 자동 가산
        if ("배송완료".equals(deliveryStatus)) {
            Integer branchId = hqInventory.getBranch().getId();
            Integer itemId = hqInventory.getItem().getId();
            Integer requestQty = hqInventory.getRequestQuantity();

            // 1튜브/박스 당 1000g 단위 가산
            int addStock = requestQty * 1000;

            BranchInventory branchInventory = branchInventoryRepository
                    .findByBranchIdWithItem(branchId)
                    .stream()
                    .filter(bi -> bi.getItem().getId().equals(itemId))
                    .findFirst()
                    .orElseGet(() -> BranchInventory.builder()
                            .branch(hqInventory.getBranch())
                            .item(hqInventory.getItem())
                            .stockLevel(0)
                            .build()
                    );

            branchInventory.updateStockLevel(addStock);
            branchInventoryRepository.save(branchInventory);

            log.info("[Delivery Service] 배송완료 및 지점 재고 반영 - 지점: {}, 상품: {}, 추가: {}g",
                    branchId, itemId, addStock);
        }

        hqInventoryRepository.save(hqInventory);
    }
}