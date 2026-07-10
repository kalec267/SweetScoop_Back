package com.sweetscoop.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sweetscoop.inventory.entity.BranchInventory;

import java.util.List;
import java.util.Optional;

public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Integer> {
    // 특정 지점의 특정 물품 재고 찾기
    Optional<BranchInventory> findByBranchIdAndItemId(Integer branchId, Integer itemId);
    
    // API 명세서: 지점별 전체 재고 조회용
    List<BranchInventory> findByBranchId(Integer branchId);
}