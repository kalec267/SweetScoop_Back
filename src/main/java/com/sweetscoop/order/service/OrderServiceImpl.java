package com.sweetscoop.order.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sweetscoop.order.dto.MenuRequestDTO;
import com.sweetscoop.order.dto.OptionRequestDTO;
import com.sweetscoop.order.dto.OrderItemRequestDTO;
import com.sweetscoop.order.dto.OrderRequestDTO;
import com.sweetscoop.order.model.OrderItemMenuVO;
import com.sweetscoop.order.model.OrderItemOptionVO;
import com.sweetscoop.order.model.OrderItemVO;
import com.sweetscoop.order.model.OrderVO;
import com.sweetscoop.order.repository.OrderDAO;
import com.sweetscoop.payment.model.PaymentVO;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private OrderDAO orderDAO;

	// 주문 생성
	@Override
	public Integer createOrder(OrderRequestDTO request) {

	    OrderVO order = new OrderVO();

	    order.setCustomerId(request.getCustomerId());
	    order.setBranchId(request.getBranchId());
	    order.setKioskId(request.getKioskId());
	    order.setOrderType(request.getOrderType());
	    order.setLanguage(request.getLanguage());
	    order.setStatus(request.getStatus());
	    order.setCreatedAt(request.getCreatedAt());
	    order.setWaitingNo(request.getWaitingNo());
	    order.setReceiptNo(request.getReceiptNo());
	    order.setTotalPrice(request.getTotalPrice());
	    order.setCouponUsed(request.getCouponUsed());

	    orderDAO.insertOrder(order);

	    Integer orderId = order.getId();

	    for (OrderItemRequestDTO item : request.getItems()) {

	        OrderItemVO orderItem = new OrderItemVO();

	        orderItem.setOrderId(orderId);
	        orderItem.setCupId(item.getCupId());
	        orderItem.setSizeId(item.getSizeId());
	        orderItem.setQuantity(item.getQuantity());
	        orderItem.setTotalPrice(item.getTotalPrice());

	        orderDAO.insertOrderItem(orderItem);

	        Integer orderItemId = orderItem.getId();

	        for (MenuRequestDTO menu : item.getMenus()) {
	            OrderItemMenuVO menuVO = new OrderItemMenuVO();

	            menuVO.setOrderItemId(orderItemId);
	            menuVO.setMenuId(menu.getMenuId());

	            orderDAO.insertOrderItemMenu(menuVO);
	        }

	        for (OptionRequestDTO option : item.getOptions()) {
	            OrderItemOptionVO optionVO = new OrderItemOptionVO();

	            optionVO.setOrderItemId(orderItemId);
	            optionVO.setMenuOptionId(option.getMenuOptionId());

	            orderDAO.insertOrderItemOption(optionVO);
	        }
	    }

	    return orderId;
	}

	// 주문 조회
	@Override
	public OrderVO getOrder(int id) {

		return orderDAO.selectOrder(id);

	}

	// 주문 목록
	@Override
	public List<OrderVO> getOrderList() {

		return orderDAO.selectOrderList();

	}

	// 주문 상태 변경
	@Override
	public void updateOrderStatus(OrderVO order) {

		orderDAO.updateOrderStatus(order);

	}

	// 주문 삭제
	@Override
	@Transactional
	public void deleteOrder(int id) {
	    // 1. 자식 및 연관 데이터부터 역순으로 삭제 처리
	    orderDAO.deleteOrderItemOptionByOrderId(id);
	    orderDAO.deleteOrderItemMenuByOrderId(id);
	    orderDAO.deleteOrderItemByOrderId(id);
	    orderDAO.deletePaymentByOrderId(id); // 💡 결제 데이터 먼저 삭제

	    // 2. 최종 부모 주문(ORDERS) 삭제
	    orderDAO.deleteOrder(id);
	}

}