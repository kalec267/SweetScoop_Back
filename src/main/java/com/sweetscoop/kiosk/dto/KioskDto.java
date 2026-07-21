package com.sweetscoop.kiosk.dto;

import com.sweetscoop.kiosk.entity.Kiosk;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KioskDto {
    private Integer id;
    private String kioskName;
    private String status;

    // 엔티티를 받아 DTO로 변환해 주는 편리한 생성자
    public KioskDto(Kiosk kiosk) {
        this.id = kiosk.getId();
        this.status = kiosk.getStatus();
    }
}