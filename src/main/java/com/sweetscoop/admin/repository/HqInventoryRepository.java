package com.sweetscoop.admin.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sweetscoop.admin.entity.HqInventory;

@Repository
public interface HqInventoryRepository extends JpaRepository<HqInventory, Integer> {

    // 1. 대시보드 요약: 승인 대기 상태 카운트
    long countByApprovalStatus(String approvalStatus);

    // 2. 대시보드 요약: 배송 중 상태 카운트
    long countByDeliveryStatus(String deliveryStatus);

    // 3. 전체 발주/배송 목록 (N+1 방지 Fetch Join + 최신순 정렬)
    @Query("select h from HqInventory h join fetch h.branch join fetch h.item order by h.id desc")
    List<HqInventory> findAllWithBranchAndItem();

    // 4. 특정 지점 발주 목록 (N+1 방지 Fetch Join)
    @Query("select h from HqInventory h join fetch h.branch join fetch h.item where h.branch.id = :branchId order by h.id desc")
    List<HqInventory> findByBranchIdWithBranchAndItem(@Param("branchId") Integer branchId);

    // 기존 메서드 유지
    List<HqInventory> findByBranch_Id(Integer branchId);
}