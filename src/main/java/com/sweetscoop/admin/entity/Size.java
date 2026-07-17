package com.sweetscoop.admin.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SIZE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT COMMENT '사이즈 ID'")
    private Integer id;

    @Column(name = "category_id", nullable = false, columnDefinition = "INT COMMENT '카테고리 ID'")
    private Integer categoryId;

    @Column(length = 50, nullable = false, columnDefinition = "VARCHAR(50) COMMENT '사이즈명(싱글,파인트 등)'")
    private String name;

    @Column(name = "flavor_cnt", nullable = false, columnDefinition = "INT COMMENT '선택 가능 맛 갯수'")
    private Integer flavorCnt;

    @Column(nullable = false, columnDefinition = "INT COMMENT '사이즈 가격'")
    private Integer price;

    @Column(name = "total_weight_g", nullable = false, columnDefinition = "INT COMMENT '해당 사이즈 총 제공 중량(g)'")
    private Integer totalWeightG;

    @Column(name = "size_img", length = 200, columnDefinition = "VARCHAR(200) COMMENT '사이즈 이미지 경로'")
    private String sizeImg;

    /**
     * 비즈니스 로직: 가격 변경 메소드
     */
    public void changePrice(Integer price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("올바르지 않은 가격 설정입니다.");
        }
        this.price = price;
    }
}