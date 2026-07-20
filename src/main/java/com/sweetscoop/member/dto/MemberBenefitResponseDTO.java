package com.sweetscoop.member.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberBenefitResponseDTO {

    private boolean member;
    private Integer memberId;
    private String phoneNumber;
    private Integer point;
    private List<CouponBenefitDTO> coupons;

    @Getter
    @Builder
    public static class CouponBenefitDTO {

        private Integer couponId;
        private String name;
        private Integer discountValue;
        private LocalDate expiryDate;
    }
}