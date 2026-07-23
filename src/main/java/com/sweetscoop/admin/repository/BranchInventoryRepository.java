package com.sweetscoop.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sweetscoop.admin.entity.BranchInventory;
import com.sweetscoop.admin.entity.BranchInventoryId; // 👈 복합키 클래스 임포트

@Repository
// ⚠️ ID 타입 자리에 Integer 대신 복합키 클래스인 'BranchInventoryId'를 넣어줍니다.
public interface BranchInventoryRepository extends JpaRepository<BranchInventory, BranchInventoryId> {
    
    @Query("select bi from BranchInventory bi join fetch bi.item where bi.branch.id = :branchId")
    List<BranchInventory> findByBranchIdWithItem(@Param("branchId") Integer branchId);
    
    Optional<BranchInventory> findByBranchIdAndItemId(Integer branchId, Integer itemId);
}