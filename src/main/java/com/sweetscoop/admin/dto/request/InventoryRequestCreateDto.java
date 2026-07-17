package com.sweetscoop.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryRequestCreateDto {
    private Integer branchId;
    private Integer itemId;
    private Integer quantity;
}