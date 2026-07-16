package com.sweetscoop.order.model;

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
public class OrderItemMenuVO {

    private Integer id;
    private Integer orderItemId;
    private Integer menuId;
}