package com.sweetscoop.sales.controller;

import com.sweetscoop.sales.service.SalesService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData(
            @RequestParam(value = "branchId", defaultValue = "0") Integer branchId,
            @RequestParam(value = "filter", defaultValue = "today") String filter,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        
        return salesService.getDashboardData(branchId, filter, startDate, endDate);
    }
    
    // 💡 2. 본사/지점 금일 전사 매출 통합 조회 API
    @GetMapping({"/today", "/dashboard/today"})
    public ResponseEntity<Map<String, Object>> getTodaySalesSummary(
            @RequestParam(value = "branchId", required = false) Integer branchId) {
        
        Map<String, Object> todaySalesData = salesService.getTodaySalesSummary(branchId);
        return ResponseEntity.ok(todaySalesData);
    }
}