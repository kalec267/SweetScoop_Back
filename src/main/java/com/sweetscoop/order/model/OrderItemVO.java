package com.sweetscoop.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemVO {

    private Integer id;
    private Integer orderId;
    private Integer cupId;
    private Integer sizeId;
    private Integer quantity;
    private Integer totalPrice;
}
