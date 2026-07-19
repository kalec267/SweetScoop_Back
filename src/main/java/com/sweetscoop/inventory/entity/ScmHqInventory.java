package com.sweetscoop.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "HQINVENTORY")
@Getter @Setter
@NoArgsConstructor
public class ScmHqInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "branch_id", nullable = false)
    private Integer branchId;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

	/*
	 * @Column(name = "hqManager_id") private String hqManagerId;
	 */

    @Column(name = "approval_status", length = 20)
    private String approvalStatus;

    @Column(name = "delivery_status", length = 20)
    private String deliveryStatus;

    @Column(name = "request_quantity", nullable = false)
    private Integer requestQuantity;
}