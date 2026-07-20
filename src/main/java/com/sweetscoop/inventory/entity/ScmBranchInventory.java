package com.sweetscoop.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BRANCHINVENTORY")
@IdClass(ScmBranchInventoryId.class)
@Getter
@Setter
@NoArgsConstructor
public class ScmBranchInventory {

    @Id
    @Column(name = "branch_id", nullable = false)
    private Integer branchId;

    @Id
    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(name = "stock_level")
    private Integer stockLevel = 0;

    // 재고 차감
    public void decreaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 수량은 0보다 커야 합니다.");
        }

        if (stockLevel == null) {
            stockLevel = 0;
        }

        if (stockLevel < amount) {
            throw new IllegalStateException(
                "지점 재고가 부족합니다. 현재 재고: " + stockLevel + "g"
            );
        }

        stockLevel -= amount;
    }

    // 재고 입고
    public void increaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("입고 수량은 0보다 커야 합니다.");
        }

        if (stockLevel == null) {
            stockLevel = 0;
        }

        stockLevel += amount;
    }
}