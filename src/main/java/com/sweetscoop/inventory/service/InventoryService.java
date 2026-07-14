package com.sweetscoop.inventory.service;

import com.sweetscoop.inventory.entity.BranchInventory;
import com.sweetscoop.inventory.entity.HqInventory;
import com.sweetscoop.inventory.entity.HqStock;
import com.sweetscoop.inventory.repository.BranchInventoryRepository;
import com.sweetscoop.inventory.repository.HqInventoryRepository;
import com.sweetscoop.inventory.repository.HqStockRepository;
import com.sweetscoop.inventory.repository.ItemRepository;
import com.sweetscoop.inventory.repository.BranchRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final BranchInventoryRepository branchInventoryRepository;
    private final HqInventoryRepository hqInventoryRepository;
    private final HqStockRepository hqStockRepository;
    private final BranchRepository branchRepository;
    private final ItemRepository itemRepository;

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
        boolean hasPendingOrder = hqInventoryRepository.existsByBranchIdAndItemIdAndApprovalStatus(branchId, itemId, "PENDING");
        
        if (!hasPendingOrder) {
            HqInventory autoOrder = new HqInventory();
            autoOrder.setBranchId(branchId);
            autoOrder.setItemId(itemId);
            autoOrder.setApprovalStatus("PENDING");
            autoOrder.setDeliveryStatus("PREPARING");
            autoOrder.setRequestQuantity(3000); 
            
            hqInventoryRepository.save(autoOrder);
        }
    }
    
 // 지점 발주 승인 로직에 본사 자동 수급 트리거 추가
    @Transactional
    public void importHqOrder(Integer hqInventoryId) {
        HqInventory hqOrder = hqInventoryRepository.findById(hqInventoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 발주 건입니다."));

        if ("ARRIVED".equals(hqOrder.getDeliveryStatus()) || "COMPLETED".equals(hqOrder.getApprovalStatus())) {
            throw new IllegalStateException("이미 처리가 완료된 발주 건입니다.");
        }

        HqStock hqStock = hqStockRepository.findByItemId(hqOrder.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("본사 창고에 등록되지 않은 물품입니다."));

        // 본사 재고 차감
        hqStock.decreaseStock(hqOrder.getRequestQuantity());
        hqStockRepository.save(hqStock);

        // 모찌 종류(13~17)는 개수 단위라 500개 미만, 아이스크림/원두는 50,000g 미만일 때 공장에 자동 주문 요청
        int threshold = (hqOrder.getItemId() >= 13 && hqOrder.getItemId() <= 17) ? 500 : 50000;
        if (hqStock.getStockLevel() < threshold) {
            triggerHqAutoOrderFromFactory(hqOrder.getItemId());
        }

        // 발주 상태 완료 처리
        hqOrder.setDeliveryStatus("ARRIVED"); 
        hqOrder.setApprovalStatus("COMPLETED");
        hqInventoryRepository.save(hqOrder);

        // 지점 재고 가산
        BranchInventory branchStock = branchInventoryRepository
                .findByBranchIdAndItemId(hqOrder.getBranchId(), hqOrder.getItemId())
                .orElseGet(() -> {
                    BranchInventory newStock = new BranchInventory();
                    newStock.setBranchId(hqOrder.getBranchId());
                    newStock.setItemId(hqOrder.getItemId());
                    newStock.setStockLevel(0);
                    return branchInventoryRepository.save(newStock);
                });

        branchStock.increaseStock(hqOrder.getRequestQuantity());
        branchInventoryRepository.save(branchStock);
    }

    // 본사 재고 부족 시 공장에 공급 요청을 보내는 자동 트리거 메서드
    private void triggerHqAutoOrderFromFactory(Integer itemId) {
        // 본사 재고는 대량으로 움직이므로 한 번 자동 보충될 때 200,000g(200kg) 또는 개수 2,000개씩 대량으로 공급을 요청합니다.
        int autoSupplyAmount = (itemId >= 13 && itemId <= 17) ? 2000 : 200000;
        
        System.out.println("🚨 [SCM 알림] 본사 창고 물품 #" + itemId + " 재고 부족 감지!");
        System.out.println("🏭 [공장 자동 발주] 공장 제조 라인에 원재료 " + autoSupplyAmount + "g/ea 자동 보충 요청 전송 완료.");
    }

    // 본사 관리자가 화면에서 직접 원자재 수동 충전 요청을 보내는 비즈니스 로직
    @Transactional
    public void chargeHqStock(Integer itemId, int amount) {
        HqStock hqStock = hqStockRepository.findByItemId(itemId)
                .orElseGet(() -> {
                    HqStock newStock = new HqStock();
                    newStock.setItemId(itemId);
                    newStock.setStockLevel(0);
                    return hqStockRepository.save(newStock);
                });
        hqStock.increaseStock(amount);
        hqStockRepository.save(hqStock);
    }

    // 한글 품명을 조인한 가공 데이터 리스트 조회
    public List<Map<String, Object>> getBranchInventoryWithNames(Integer branchId) {
        List<Object[]> rawData = branchInventoryRepository.findInventoryWithItemName(branchId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> map = new HashMap<>();
            map.put("itemId", row[0]);
            map.put("itemName", row[1]);      
            map.put("categoryName", row[2]);  
            map.put("stockLevel", row[3]);    
            map.put("unit", row[4]);          
            result.add(map);
        }
        return result;
    }

    // 수동 발주 신청
    @Transactional
    public void createManualOrder(Integer branchId, Integer itemId, int amount) {
        HqInventory manualOrder = new HqInventory();
        manualOrder.setBranchId(branchId);
        manualOrder.setItemId(itemId);
        manualOrder.setApprovalStatus("PENDING");   
        manualOrder.setDeliveryStatus("PREPARING");
        manualOrder.setRequestQuantity(amount);     
        
        hqInventoryRepository.save(manualOrder);
    }
    
    public List<Map<String, Object>> getAllHqOrdersWithNames() {
        List<HqInventory> rawOrders = hqInventoryRepository.findAll();
        
        // 데이터베이스의 BRANCH 테이블 데이터를 동적으로 읽어와 맵핑 테이블 구성
        Map<Integer, String> branchNameMap = new HashMap<>();
        branchRepository.findAll().forEach(b -> branchNameMap.put(b.getId(), b.getBranchName()));

        Map<Integer, String> itemNameMap = new HashMap<>();
        itemRepository.findAll().forEach(item -> itemNameMap.put(item.getId(), item.getItemName()));
        
        List<Map<String, Object>> result = new ArrayList<>();

        for (HqInventory order : rawOrders) {
            Map<String, Object> map = new HashMap<>();
            map.put("hqInventoryId", order.getId());
            map.put("branchId", order.getBranchId());
            
            String branchName = branchNameMap.getOrDefault(order.getBranchId(), "미등록 지점(#" + order.getBranchId() + ")");
            map.put("branchName", branchName); 
            
            map.put("itemId", order.getItemId());
            String itemName = itemNameMap.getOrDefault(order.getItemId(), "미등록 원자재(#" + order.getItemId() + ")");
            map.put("itemName", itemName); 
            map.put("requestQuantity", order.getRequestQuantity());
            map.put("approvalStatus", order.getApprovalStatus()); 
            map.put("deliveryStatus", order.getDeliveryStatus());   
            result.add(map);
        }
        return result;
    }
}