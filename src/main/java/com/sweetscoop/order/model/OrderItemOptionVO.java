package com.sweetscoop.order.model;

import java.time.LocalDateTime;

import com.sweetscoop.payment.model.PaymentVO;

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
public class OrderItemOptionVO {

    private Integer id;
    private Integer orderItemId;
    private Integer menuOptionId;
}