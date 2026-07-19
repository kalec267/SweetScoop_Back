package com.sweetscoop.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BRANCHINVENTORY")
@Getter @Setter
@NoArgsConstructor
public class ScmBranchInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "branch_id", nullable = false)
    private Integer branchId;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(name = "stock_level")
    private Integer stockLevel = 0;

    // 	재고 차감
    public void decreaseStock(int amount) {
        if (this.stockLevel < amount) {
            throw new IllegalStateException("지점 재고가 부족합니다. 현재 재고: " + this.stockLevel + "g");
        }
        this.stockLevel -= amount;
    }

    // 재고 입고
    public void increaseStock(int amount) {
        this.stockLevel += amount;
    }
}