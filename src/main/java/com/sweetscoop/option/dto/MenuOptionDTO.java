package com.sweetscoop.option.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuOptionDTO {

    private Integer id;
    private Integer categoryId;
    private String name;
    private Integer price;
    private Boolean isActive;
}