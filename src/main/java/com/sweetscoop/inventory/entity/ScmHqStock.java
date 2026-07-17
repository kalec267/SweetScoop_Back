package com.sweetscoop.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "HQ_STOCK")
@Getter @Setter
@NoArgsConstructor
public class ScmHqStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "item_id", nullable = false, unique = true)
    private Integer itemId;

    @Column(name = "stock_level")
    private Integer stockLevel = 0;

    // 본사 입고 (원재료 수급용)
    public void increaseStock(int amount) {
        this.stockLevel += amount;
    }

    // 본사 출고 (지점 발주 승인 시 차감)
    public void decreaseStock(int amount) {
        if (this.stockLevel < amount) {
            throw new IllegalStateException("본사 창고 재고가 부족합니다. (현재 재고: " + this.stockLevel + "g)");
        }
        this.stockLevel -= amount;
    }
}