package com.sweetscoop.size.dto;

import lombok.Data;

@Data
public class SizeDTO {

    private Integer id;
    private Integer categoryId;
    private String name;
    private Integer flavorCnt;
    private Integer price;
    private Integer totalWeightG;
    private String sizeImg;

}