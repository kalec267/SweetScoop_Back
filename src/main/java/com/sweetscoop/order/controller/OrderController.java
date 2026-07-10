package com.sweetscoop.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sweetscoop.order.dto.OrderRequestDTO;
import com.sweetscoop.order.model.OrderVO;
import com.sweetscoop.order.service.OrderService;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(
    		@RequestBody OrderRequestDTO request) {


    	orderService.createOrder(request);


        return ResponseEntity.ok("주문 저장 성공");
    }

}