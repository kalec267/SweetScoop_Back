package com.sweetscoop.order.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderItemRequestDTO {

	 /*
     * ORDERITEM INSERT 후 생성되는 PK
     */
    private Integer id;

    /*
     * ORDERS 테이블의 주문 ID
     */
    private Integer orderId;

    private Integer cupId;

    private Integer sizeId;


    private Integer quantity;


    private Integer totalPrice;


    private List<MenuRequestDTO> menus;


    private List<OptionRequestDTO> options;
    

}
