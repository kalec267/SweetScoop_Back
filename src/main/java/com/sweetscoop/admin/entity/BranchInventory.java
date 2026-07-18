package com.sweetscoop.admin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sweetscoop.branch.entity.Branch;
import com.sweetscoop.item.entity.Item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BRANCHINVENTORY")
@IdClass(BranchInventoryId.class) // 👈 복합키 클래스 매핑
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BranchInventory {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false, columnDefinition = "INT COMMENT '지점 ID'")
    private Branch branch;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, columnDefinition = "INT COMMENT '물품 ID'")
    private Item item;

    @Column(name = "stock_level", columnDefinition = "INT DEFAULT 0 COMMENT '현재 재고량 (단위: g 등)'")
    private Integer stockLevel;

    /**
     * 비즈니스 로직: 재고 변동 (주문 시 차감 또는 발주 입고 시 추가)
     */
    public void updateStockLevel(Integer amount) {
        if (this.stockLevel + amount < 0) {
            throw new IllegalArgumentException("재고는 0 미만이 될 수 없습니다.");
        }
        this.stockLevel += amount;
    }
}