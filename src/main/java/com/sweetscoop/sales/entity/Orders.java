package com.sweetscoop.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "ORDERS")
@Getter @Setter
@NoArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "branch_id", nullable = false)
    private Integer branchId;

    @Column(name = "kiosk_id", nullable = false)
    private Integer kiosk_id;

    @Column(name = "order_type", nullable = false)
    private String orderType;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "status", nullable = false)
    private String status; // '결제완료', '준비중', '완료' 등

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 주문 발생 시각 (시간대별 통계의 핵심)

    @Column(name = "waiting_no")
    private Integer waitingNo;

    @Column(name = "receipt_no", nullable = false)
    private String receiptNo;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice; // 주문 총 금액

    @Column(name = "coupon_used")
    private Boolean couponUsed;
}