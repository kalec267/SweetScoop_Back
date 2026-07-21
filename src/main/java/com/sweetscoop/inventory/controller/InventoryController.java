package com.sweetscoop.inventory.controller;

import com.sweetscoop.inventory.entity.ScmBranch;
import com.sweetscoop.inventory.entity.ScmHqStock;
import com.sweetscoop.inventory.entity.ScmItem;
import com.sweetscoop.inventory.repository.ScmBranchRepository;
import com.sweetscoop.inventory.repository.ScmHqStockRepository;
import com.sweetscoop.inventory.repository.ScmItemRepository;
import com.sweetscoop.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ScmHqStockRepository hqStockRepository;
    private final ScmBranchRepository branchRepository;
    private final ScmItemRepository itemRepository;

    
    // 전체 원자재(Item) 목록 조회
    @GetMapping("/items")
    public ResponseEntity<List<ScmItem>> getAllItems() {
        return ResponseEntity.ok(itemRepository.findAll());
    }
    
    // 지점별 재고 조회: 한글 품명과 카테고리가 맵 리스트 형태로 변환되어 프론트로
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Map<String, Object>>> getBranchInventory(@PathVariable("branchId") Integer branchId) {
        return ResponseEntity.ok(inventoryService.getBranchInventoryWithNames(branchId));
    }

    // 화면에서 [수동 신청] 버튼을 눌렀을 때 본사 발주 대기열에 추가
    @PostMapping("/request/manual")
    public ResponseEntity<String> createManualRequest(@RequestParam("branchId") Integer branchId, 
                                                      @RequestParam("itemId") Integer itemId, 
                                                      @RequestParam("amount") int amount) {
        inventoryService.createManualOrder(branchId, itemId, amount);
        return ResponseEntity.ok("본사 발주 대기열에 수동 신청이 정상 등록되었습니다.");
    }

    @PostMapping("/in")
    public ResponseEntity<String> stockIn(@RequestParam("branchId") Integer branchId, 
                                          @RequestParam("itemId") Integer itemId, 
                                          @RequestParam("amount") int amount) {
        inventoryService.importStock(branchId, itemId, amount);
        return ResponseEntity.ok("입고 완료");
    }

    @PostMapping("/in/hq")
    public ResponseEntity<String> stockInHqOrder(@RequestParam("hqInventoryId") Integer hqInventoryId) {
        try {
            inventoryService.importHqOrder(hqInventoryId); 
            return ResponseEntity.ok("본사 발주 물품 입고 처리가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/out")
    public ResponseEntity<String> stockOut(@RequestParam("branchId") Integer branchId, 
                                           @RequestParam("itemId") Integer itemId, 
                                           @RequestParam("amount") int amount) {
        inventoryService.exportStock(branchId, itemId, amount);
        return ResponseEntity.ok("출고 완료");
    }
    
 // 본사 창고 자체에 재고를 채워넣는 API (공장 수급용)
    @PostMapping("/hq/stock/charge")
    public ResponseEntity<String> chargeHqStock(@RequestParam("itemId") Integer itemId, 
                                                @RequestParam("amount") int amount) {
        try {
            ScmHqStock hqStock = hqStockRepository.findByItemId(itemId)
                    .orElseGet(() -> {
                        ScmHqStock newStock = new ScmHqStock();
                        newStock.setItemId(itemId);
                        newStock.setStockLevel(0);
                        return hqStockRepository.save(newStock);
                    });
            
            hqStock.increaseStock(amount);
            hqStockRepository.save(hqStock);
            
            return ResponseEntity.ok("본사 창고에 원자재 " + amount + "g/ea가 성공적으로 입고(충전)되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("본사 재고 충전 실패: " + e.getMessage());
        }
    }
    
    @PostMapping("/out/order")
    public ResponseEntity<String> deductStockForOrder(
            @RequestParam("branchId") Integer branchId,
            @RequestParam("sizeId") Integer sizeId,
            @RequestBody List<Integer> selectedMenuIds) { // 키오스크에서 보낸 메뉴(맛) ID 리스트 [1, 1, 3]
        try {
            inventoryService.exportStockForOrder(branchId, sizeId, selectedMenuIds);
            return ResponseEntity.ok("주문 재고 분할 차감 및 자동 발주 검사가 성공적으로 처리되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("재고 차감 실패: " + e.getMessage());
        }
    }
    
    // 본사 관리자용: 전체 발주 신청 내역 조회 API
    @GetMapping("/hq/orders")
    public ResponseEntity<List<Map<String, Object>>> getAllHqOrders() {
        return ResponseEntity.ok(inventoryService.getAllHqOrdersWithNames());
    }

    // 본사 실시간 창고 재고 조회 API
    @GetMapping("/hq/stock")
    public ResponseEntity<?> getHqStockList() {
        try {
            List<ScmHqStock> stocks = hqStockRepository.findAll();
            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("본사 창고 재고 목록을 불러오는 중 에러가 발생했습니다: " + e.getMessage());
        }
    }
    
    //전체 지점 목록 조회 API
    @GetMapping("/branches")
    public ResponseEntity<List<ScmBranch>> getAllBranches() {
        // DB의 BRANCH 테이블에 있는 모든 레코드를 정렬(ID순)해서 그대로 반환합니다.
        return ResponseEntity.ok(branchRepository.findAll());
    }
    
    
}