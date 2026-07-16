package com.sweetscoop.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sweetscoop.admin.entity.HqInventory;

@Repository
public interface HqInventoryRepository extends JpaRepository<HqInventory, Integer> {

    // 1. 대시보드 요약: 처리 대기 상태(예: '대기중')인 건수 카운트
    long countByApprovalStatus(String approvalStatus);

    // 2. 대시보드 요약: 배송 중 상태(예: '배송중')인 건수 카운트
    long countByDeliveryStatus(String deliveryStatus);

    // 3. 대시보드 테이블: N+1 문제를 방지하기 위해 지점(Branch)과 물품(Item)을 한 번에 fetch join으로 가져오기
    @Query("select h from HqInventory h join fetch h.branch join fetch h.item order by h.id desc")
    List<HqInventory> findAllWithBranchAndItem();
    
    List<HqInventory> findByBranch_Id(Integer branchId);
}