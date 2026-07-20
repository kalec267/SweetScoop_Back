package com.sweetscoop.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCalculationRequestDTO {

    private String phoneNumber;
    private Integer originalAmount;
    private Integer pointUsed;
    private Integer couponId;
}