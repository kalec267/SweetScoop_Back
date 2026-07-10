package com.sweetscoop.inventory.service;

import com.sweetscoop.inventory.entity.BranchInventory;
import com.sweetscoop.inventory.entity.HqInventory;
import com.sweetscoop.inventory.repository.BranchInventoryRepository;
import com.sweetscoop.inventory.repository.HqInventoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final BranchInventoryRepository branchInventoryRepository;
    private final HqInventoryRepository hqInventoryRepository;

    // 지점별 재고 조회
    public List<BranchInventory> getBranchInventory(Integer branchId) {
        return branchInventoryRepository.findByBranchId(branchId);
    }

    // 재고 입고
    @Transactional
    public void importStock(Integer branchId, Integer itemId, int amount) {
        BranchInventory inventory = branchInventoryRepository.findByBranchIdAndItemId(branchId, itemId)
                .orElseGet(() -> {
                    BranchInventory newInv = new BranchInventory();
                    newInv.setBranchId(branchId);
                    newInv.setItemId(itemId);
                    return branchInventoryRepository.save(newInv);
                });
        inventory.increaseStock(amount);
    }

    // 재고 출고 및 자동 발주 검사
    @Transactional
    public void exportStock(Integer branchId, Integer itemId, int amount) {
        BranchInventory inventory = branchInventoryRepository.findByBranchIdAndItemId(branchId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("재고 정보가 없습니다."));
        
        inventory.decreaseStock(amount);

        // 재고 임계치 미만일 때 자동 발주
        if (inventory.getStockLevel() < 5000) {
            checkAndTriggerAutoOrder(branchId, itemId);
        }
    }

    // 내부 자동 발주 트리거 메서드
    private void checkAndTriggerAutoOrder(Integer branchId, Integer itemId) {
        // 이미 본사에 요청해서 대기 중인 발주건이 없을 때만 신규 등록
        boolean hasPendingOrder = hqInventoryRepository.existsByBranchIdAndItemIdAndApprovalStatus(branchId, itemId, "PENDING");
        
        if (!hasPendingOrder) {
            HqInventory autoOrder = new HqInventory();
            autoOrder.setBranchId(branchId);
            autoOrder.setItemId(itemId);
            autoOrder.setApprovalStatus("PENDING");
            autoOrder.setDeliveryStatus("PREPARING");
            autoOrder.setRequestQuantity(3000); // 기본 발주 단위
            
            hqInventoryRepository.save(autoOrder);
        }
    }
    
    @Transactional
    public void importHqOrder(Integer hqInventoryId) {
        // 1. 발주 대기 건 조회
        HqInventory hqOrder = hqInventoryRepository.findById(hqInventoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 발주 건입니다."));

        // 2. 이미 배송 완료된 건인지 검증
        if ("ARRIVED".equals(hqOrder.getDeliveryStatus())) {
            throw new IllegalStateException("이미 입고 완료 처리된 발주 건입니다.");
        }

        // 3. 발주 상태를 '배송 완료' 상태로 변경
        hqOrder.setDeliveryStatus("ARRIVED"); 
        hqOrder.setApprovalStatus("COMPLETED");
        hqInventoryRepository.save(hqOrder);

        // 4. 해당 매장의 지점 재고 테이블 조회 및 수량 증가
        BranchInventory branchStock = branchInventoryRepository
                .findByBranchIdAndItemId(hqOrder.getBranchId(), hqOrder.getItemId())
                .orElseGet(() -> {
                    BranchInventory newStock = new BranchInventory();
                    newStock.setBranchId(hqOrder.getBranchId());
                    newStock.setItemId(hqOrder.getItemId());
                    newStock.setStockLevel(0);
                    return branchInventoryRepository.save(newStock);
                });

        // 엔티티에 정의된 기존 비즈니스 메서드(increaseStock) 활용하여 수량 누적
        branchStock.increaseStock(hqOrder.getRequestQuantity());
        branchInventoryRepository.save(branchStock);
    }
}