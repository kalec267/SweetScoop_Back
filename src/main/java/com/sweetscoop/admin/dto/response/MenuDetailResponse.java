package com.sweetscoop.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuDetailResponse {
    private Integer id;
    private String categoryName; // CATEGORY 테이블 조인 결과
    private String itemName;     // ITEM 테이블 조인 결과
    private String name;
    private String menuImg;
}