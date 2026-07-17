package com.sweetscoop.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sweetscoop.inventory.entity.BranchInventory;

import java.util.List;
import java.util.Optional;

public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Integer> {
    
    // 특정 지점의 특정 물품 재고 찾기
    Optional<BranchInventory> findByBranchIdAndItemId(Integer branchId, Integer itemId);
    
    // 지점별 전체 재고 조회
    List<BranchInventory> findByBranchId(Integer branchId);

    // ITEM 및 CATEGORY와 JOIN하여 한글 이름까지 한 번에 가져오는 쿼리
    @Query(value = "SELECT i.id AS itemId, i.item_name AS itemName, c.name AS categoryName, bi.stock_level AS stockLevel, i.unit AS unit " +
                   "FROM BRANCHINVENTORY bi " +
                   "JOIN ITEM i ON bi.item_id = i.id " +
                   "JOIN CATEGORY c ON i.category_id = c.id " +
                   "WHERE bi.branch_id = :branchId " +
                   "ORDER BY c.id ASC, i.item_name ASC", 
           nativeQuery = true)
    List<Object[]> findInventoryWithItemName(@Param("branchId") Integer branchId);
}