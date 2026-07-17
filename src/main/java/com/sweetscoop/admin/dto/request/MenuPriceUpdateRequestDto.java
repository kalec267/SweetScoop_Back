package com.sweetscoop.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MenuPriceUpdateRequestDto {
    private Integer price;      // 변경할 사이즈 가격
}