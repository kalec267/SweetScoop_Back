package com.sweetscoop.order.repository;

import java.util.List;

import com.sweetscoop.order.model.OrderItemMenuVO;
import com.sweetscoop.order.model.OrderItemOptionVO;
import com.sweetscoop.order.model.OrderItemVO;
import com.sweetscoop.order.model.OrderVO;
import com.sweetscoop.payment.model.PaymentVO;

public interface OrderDAO {

    // 주문 저장
    int insertOrder(OrderVO order);

    // 주문 상품 저장
    int insertOrderItem(OrderItemVO orderItem);

    // 주문 맛 저장
    int insertOrderItemMenu(OrderItemMenuVO orderItemMenu);

    // 주문 옵션 저장
    int insertOrderItemOption(OrderItemOptionVO orderItemOption);

    // 결제 저장
    int insertPayment(PaymentVO payment);

    // 주문 조회
    OrderVO selectOrder(int id);

    // 주문 전체 조회
    List<OrderVO> selectOrderList();

    // 주문 상태 변경
    int updateOrderStatus(OrderVO order);

    // 주문 삭제(취소)
    int deleteOrder(int id);
}
