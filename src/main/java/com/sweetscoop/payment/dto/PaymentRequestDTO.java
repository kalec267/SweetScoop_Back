package com.sweetscoop.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentRequestDTO {

    /**
     * ORDERS 테이블의 실제 PK
     */
    private String orderId;

    /**
     * Toss 결제창 요청에 사용한 고유 주문번호
     */
    private String tossOrderId;

    /**
     * Toss가 발급한 결제 승인 키
     */
    private String paymentKey;

    /**
     * 실제 Toss 승인 요청 금액
     */
    private int amount;

    /**
     * 결제 수단
     */
    private String method;

    /**
     * 카드사 또는 간편결제사
     */
    private String cardCompany;

    /**
     * 회원 전화번호
     */
    private String phoneNumber;

    /**
     * 사용한 쿠폰
     */
    private Integer couponId;

    /**
     * 사용한 포인트
     */
    private Integer pointUsed;

    /**
     * 결제 후 적립 포인트
     */
    private Integer pointEarned;
}

