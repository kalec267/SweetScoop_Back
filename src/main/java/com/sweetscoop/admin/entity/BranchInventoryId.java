package com.sweetscoop.admin.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BranchInventoryId implements Serializable {
    private Integer branch; // BranchInventory 엔티티의 필드명과 일치해야 함
    private Integer item;   // BranchInventory 엔티티의 필드명과 일치해야 함
}