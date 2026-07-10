package com.sweetscoop.payment.service;

import com.sweetscoop.payment.dto.OrderCreateRequestDTO;
import com.sweetscoop.payment.repository.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderMapper orderMapper;

    @Transactional(rollbackFor = Exception.class)
    public int createOrder(OrderCreateRequestDTO dto) throws Exception {
        int result = orderMapper.insertOrder(dto);
        if (result <= 0) {
            throw new Exception("ORDERS 테이블에 주문서를 생성하지 못했습니다.");
        }
        return dto.getId(); // 발급된 Auto Increment id 리턴
    }
}