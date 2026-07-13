package com.sweetscoop.order.dto;

/*
 * 목적: Vue에서 넘어오는 전체 주문 데이터 저장
 */

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class OrderRequestDTO {

	// 주문자 정보
	private Integer customerId;

	// 지점 정보
	private Integer branchId;

	// 키오스크 정보
	private Integer kioskId;

	// 주문 정보
	private String orderType;

	private String language;

	private String status;

	// 시간 / 대기번호 / 영수증
	private LocalDateTime createdAt;

	private Integer waitingNo;

	private String receiptNo;

	// 금액 정보
	private Integer totalPrice;

	// 쿠폰 사용 여부
	private Boolean couponUsed;

	// 주문 상품 목록
	private List<OrderItemRequestDTO> items;

	// 결제 정보
	private PaymentRequestDTO payment;
	
//	컵 사이즈
	private Integer cupId;

}