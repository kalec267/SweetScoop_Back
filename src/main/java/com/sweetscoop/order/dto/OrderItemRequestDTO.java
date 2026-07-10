package com.sweetscoop.order.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderItemRequestDTO {


    private Integer cupId;

    private Integer sizeId;


    private Integer quantity;


    private Integer totalPrice;


    private List<MenuRequestDTO> menus;


    private List<OptionRequestDTO> options;

}
