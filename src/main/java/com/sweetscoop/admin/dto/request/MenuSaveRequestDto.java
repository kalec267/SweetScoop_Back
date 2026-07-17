package com.sweetscoop.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MenuSaveRequestDto {
    private Integer categoryId; // 카테고리 ID (아이스크림, 음료 등)
    private Integer itemId;     // 매핑할 물류 ID
    private String name;        // 메뉴/맛 이름 (예: 엄마는 외계인)
    private String menuImg;     // 이미지 URL
}