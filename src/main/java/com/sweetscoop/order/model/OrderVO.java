package com.sweetscoop.order.model;

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

public class OrderVO {

    private Integer id;
    private Integer customerId;
    private Integer branchId;
    private Integer kioskId;
    private String orderType;
    private String language;
    private String status;
    private LocalDateTime createdAt;
    private Integer waitingNo;
    private String receiptNo;
    private Integer totalPrice;
    private Boolean couponUsed;

    
}