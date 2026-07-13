package com.sweetscoop.admin.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "HQINVENTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HqInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 지점 테이블과 다대일 연관관계 맵핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    // 물품 테이블과 다대일 연관관계 맵핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "hqManager_id", length = 50)
    private String hqManagerId;

    @Column(name = "approval_status", length = 20)
    private String approvalStatus; // 예: 대기중, 승인완료, 반려

    @Column(name = "delivery_status", length = 20)
    private String deliveryStatus; // 예: 준비중, 배송중, 완료

    @Column(name = "request_quantity", nullable = false)
    private Integer requestQuantity;

    // 비즈니스 로직: 승인/반려 상태 변경 메소드
    public void updateApprovalStatus(String approvalStatus, String hqManagerId) {
        this.approvalStatus = approvalStatus;
        this.hqManagerId = hqManagerId;
    }

    // 비즈니스 로직: 배송 상태 변경 메소드
    public void updateDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}