package com.sweetscoop.order.service;

import java.util.List;

import com.sweetscoop.order.dto.OrderRequestDTO;
import com.sweetscoop.order.model.OrderVO;

public interface OrderService {

    Integer createOrder(OrderRequestDTO request);

    OrderVO getOrder(int id);

    List<OrderVO> getOrderList();

    void updateOrderStatus(OrderVO order);

    void deleteOrder(int id);
}