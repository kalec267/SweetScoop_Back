package com.sweetscoop.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sweetscoop.inventory.entity.ScmHqInventory;

public interface ScmHqInventoryRepository extends JpaRepository<ScmHqInventory, Integer> {

    boolean existsByBranchIdAndItemIdAndApprovalStatus(Integer branchId, Integer itemId, String approvalStatus);

    @Modifying
    @Query("UPDATE ScmHqInventory h SET h.deliveryStatus = :deliveryStatus WHERE h.id = :id")
    int updateDeliveryStatusDirect(@Param("id") Integer id, @Param("deliveryStatus") String deliveryStatus);
}