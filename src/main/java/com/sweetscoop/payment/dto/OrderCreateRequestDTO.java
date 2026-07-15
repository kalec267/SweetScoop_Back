package com.sweetscoop.payment.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderCreateRequestDTO {
    private int id; // MyBatis에서 채워줄 Auto Increment PK
    
    // DB의 NOT NULL 제약조건을 우회 및 초기 등록 기초 데이터와 매핑하기 위한 기본값
    private int customerId;         // SQL로 넣으신 '비회원' ID
    private int branchId;           // SQL로 넣으신 '달콤강남점' ID
    private int kioskId;            // SQL로 넣으신 '정상' 키오스크 ID
    private String orderType;
    private String language;  // SQL 스펙과 일치
    private int totalPrice;             // 프론트엔드가 보낸 총 장바구니 금액
}