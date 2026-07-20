package com.sweetscoop.inventory.repository;

import com.sweetscoop.inventory.entity.ScmBranchInventory;
import com.sweetscoop.inventory.entity.ScmBranchInventoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScmBranchInventoryRepository
        extends JpaRepository<ScmBranchInventory, ScmBranchInventoryId> {

    // 특정 지점의 특정 물품 재고 찾기
    Optional<ScmBranchInventory> findByBranchIdAndItemId(
            Integer branchId,
            Integer itemId
    );

    // 지점별 전체 재고 조회
    List<ScmBranchInventory> findByBranchId(Integer branchId);

    // ITEM 및 CATEGORY와 JOIN하여 이름까지 조회
    @Query(value = """
        SELECT
            i.id AS itemId,
            i.item_name AS itemName,
            c.name AS categoryName,
            bi.stock_level AS stockLevel,
            i.unit AS unit
        FROM BRANCHINVENTORY bi
        JOIN ITEM i
            ON bi.item_id = i.id
        JOIN CATEGORY c
            ON i.category_id = c.id
        WHERE bi.branch_id = :branchId
        ORDER BY c.id ASC, i.item_name ASC
        """, nativeQuery = true)
    List<Object[]> findInventoryWithItemName(
            @Param("branchId") Integer branchId
    );
}