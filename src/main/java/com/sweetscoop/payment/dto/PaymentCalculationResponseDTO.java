package com.sweetscoop.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCalculationResponseDTO {

    private Integer originalAmount;
    private Integer pointDiscount;
    private Integer couponDiscount;
    private Integer totalDiscount;
    private Integer finalAmount;
    private Integer remainingPoint;
}