package com.sweetscoop.order.service;

import java.util.List;

import com.sweetscoop.order.dto.OrderRequestDTO;
import com.sweetscoop.order.model.OrderVO;

public interface OrderService {

    // 주문 생성
	void createOrder(OrderRequestDTO request);

    // 주문 조회
    OrderVO getOrder(int id);

    // 주문 목록
    List<OrderVO> getOrderList();

    // 주문 상태 변경
    void updateOrderStatus(OrderVO order);

    // 주문 취소
    void deleteOrder(int id);
}