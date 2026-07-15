package com.sweetscoop.order.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.order.dto.OrderRequestDTO;
import com.sweetscoop.order.service.OrderService;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody OrderRequestDTO request) {

        Integer orderId = orderService.createOrder(request);

        return ResponseEntity.ok(
            Map.of(
                "orderId", orderId,
                "receiptNo", request.getReceiptNo()
            )
        );
    }

}