package com.sweetscoop.coupon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CouponCreateRequestDto {
	private Integer memberId;
    private String name;            // 쿠폰명 (예: [여름특가] 파인트 2,000원 할인)
    private Double discountValue;   // 할인 금액/비율 (예: 2000.0)
    private Integer validDays;      // 유효기간(일수) (예: 30 -> 30일 후 만료)
}