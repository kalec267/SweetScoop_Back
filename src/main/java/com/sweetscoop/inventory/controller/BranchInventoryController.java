package com.sweetscoop.inventory.controller;

import com.sweetscoop.inventory.repository.ScmBranchInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/inventory")
public class BranchInventoryController {

    @Autowired
    private ScmBranchInventoryRepository branchInventoryRepository;

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Map<String, Object>>> getBranchInventory(@PathVariable("branchId") Integer branchId) {
        // 수정된 레포지토리 메서드 호출
        List<Object[]> rawData = branchInventoryRepository.findInventoryWithItemName(branchId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> map = new HashMap<>();
            map.put("itemId", row[0]);
            map.put("itemName", row[1]);      // 실물 이름 매칭
            map.put("categoryName", row[2]);  // 카테고리명 매칭
            map.put("stockLevel", row[3]);    // 현재 재고량
            map.put("unit", row[4]);          // 입고 단위
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
}