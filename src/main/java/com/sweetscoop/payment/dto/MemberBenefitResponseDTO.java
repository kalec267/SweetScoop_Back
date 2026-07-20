package com.sweetscoop.payment.dto;

import java.util.List;

import com.sweetscoop.coupon.dto.CouponDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberBenefitResponseDTO {

    private boolean member;

    private Integer memberId;

    private String phoneNumber;

    private Integer point;

    private List<CouponDto> coupons;
}

