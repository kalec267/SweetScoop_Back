package com.sweetscoop.payment.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentRequestDTO {
    private String orderId;            // 데이터베이스 ORDERS 테이블의 실 PK 번호
    private String tossOrderId;     // 토스 결제창 요청 시 부여했던 유니크 문자열 ID
    private String paymentKey;      // 토스가 발급해 준 일회용 인증 키
    private int amount;             // 최종 결제 금액 확인용
    private String method;          // 결제 수단 (카드/간편결제)
    private String cardCompany;     // 토스 승인 성공 후 반환받아 채워질 카드사명
    private Integer couponId = null; 
}