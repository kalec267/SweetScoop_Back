package com.sweetscoop.menu.dto;

import lombok.Data;

@Data
public class MenuDTO {

    private Integer id;
    private Integer categoryId;
    private Integer itemId;
    private String name;
    private Integer price;
    private String menuImg;
    private String categoryName;
}