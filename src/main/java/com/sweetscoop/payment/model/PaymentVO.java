package com.sweetscoop.payment.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentVO {

    private Integer id;
    private Integer orderId;
    private Integer couponId;
    private String method;
    private Integer amount;
    private String cardCompany;
    private LocalDateTime paymentTime;
    private String paymentStatus;
    private String pgTransactionId;
    private Integer pointUsed;
    private Integer pointEarned;
}