package com.sweetscoop.sales.controller;

import com.sweetscoop.sales.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 프론트 연동 허용
public class SalesController {

    private final SalesService salesService;

    // 지점 대시보드 데이터 통합 조회 API
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestParam("branchId") Integer branchId,
            @RequestParam(value = "filter", defaultValue = "today") String filter) {
        
        Map<String, Object> data = salesService.getDashboardData(branchId, filter);
        return ResponseEntity.ok(data);
    }
}