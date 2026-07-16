package com.sweetscoop.order.dto;

import lombok.Data;


@Data
public class PaymentRequestDTO {


    private String paymentMethod;


    private Integer amount;

}