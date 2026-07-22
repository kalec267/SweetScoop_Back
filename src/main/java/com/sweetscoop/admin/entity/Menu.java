package com.sweetscoop.admin.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MENU")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT COMMENT '메뉴 ID'") // 👈 수정
    private Integer id;

    @Column(name = "category_id", nullable = false, columnDefinition = "INT COMMENT '카테고리 ID'") // 👈 수정
    private Integer categoryId;

    @Column(name = "item_id", nullable = false, columnDefinition = "INT COMMENT '물류 ID'") // 👈 수정
    private Integer itemId;

    @Column(length = 30, nullable = false, columnDefinition = "VARCHAR(30) COMMENT '메뉴/맛 이름(엄마는 외계인 등)'") // 👈 수정
    private String name;

    @Column(name = "menu_img", length = 500, columnDefinition = "VARCHAR(500) COMMENT '메뉴 이미지 경로'") // 👈 수정
    private String menuImg;
    
    private Integer price;

    // 정보 수정을 위한 도메인 비즈니스 메서드
    public void updateMenuDetails(Integer categoryId, Integer itemId, String name, String menuImg) {
        this.categoryId = categoryId;
        this.itemId = itemId;
        this.name = name;
        this.menuImg = menuImg;
    }
    
    // Menu 가격 변경 비즈니스 메서드
    public void updatePrice(Integer price) {
        if (price != null && price < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }
        this.price = price;
    }
}