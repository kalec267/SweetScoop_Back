package com.sweetscoop.payment.controller;

import com.sweetscoop.payment.dto.OrderCreateRequestDTO;
import com.sweetscoop.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody OrderCreateRequestDTO dto) {
        Map<String, Object> res = new HashMap<>();
        try {
            int orderId = orderService.createOrder(dto);
            res.put("success", true);
            res.put("orderId", orderId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}