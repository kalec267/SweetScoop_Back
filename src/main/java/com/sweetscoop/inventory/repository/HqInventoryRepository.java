package com.sweetscoop.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sweetscoop.inventory.entity.HqInventory;

public interface HqInventoryRepository extends JpaRepository<HqInventory, Integer> {
    // 자동 발주 중복 방지 체크용
    boolean existsByBranchIdAndItemIdAndApprovalStatus(Integer branchId, Integer itemId, String approvalStatus);
}