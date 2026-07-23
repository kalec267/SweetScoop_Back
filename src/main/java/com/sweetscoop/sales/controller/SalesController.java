package com.sweetscoop.sales.controller;

import com.sweetscoop.sales.service.SalesService;
import lombok.RequiredArgsConstructor;
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
}