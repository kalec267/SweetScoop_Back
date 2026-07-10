package com.sweetscoop.inventory.controller;

import com.sweetscoop.inventory.entity.BranchInventory;
import com.sweetscoop.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class InventoryController {

    private final InventoryService inventoryService;

    // 지점별 재고 조회
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<BranchInventory>> getBranchInventory(@PathVariable("branchId") Integer branchId) {
        return ResponseEntity.ok(inventoryService.getBranchInventory(branchId));
    }

    // 단순 재고 입고
    @PostMapping("/in")
    public ResponseEntity<String> stockIn(@RequestParam("branchId") Integer branchId, 
                                          @RequestParam("itemId") Integer itemId, 
                                          @RequestParam("amount") int amount) {
        inventoryService.importStock(branchId, itemId, amount);
        return ResponseEntity.ok("입고 완료");
    }

    // 본사 자동 발주 건 기반 매장 입고 처리
    @PostMapping("/in/hq")
    public ResponseEntity<String> stockInHqOrder(@RequestParam("hqInventoryId") Integer hqInventoryId) {
        try {
            inventoryService.importHqOrder(hqInventoryId); 
            return ResponseEntity.ok("본사 발주 물품 입고 처리가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("입고 실패: " + e.getMessage());
        }
    }

    // 재고 출고
    @PostMapping("/out")
    public ResponseEntity<String> stockOut(@RequestParam("branchId") Integer branchId, 
                                           @RequestParam("itemId") Integer itemId, 
                                           @RequestParam("amount") int amount) {
        inventoryService.exportStock(branchId, itemId, amount);
        return ResponseEntity.ok("출고 완료");
    }
}