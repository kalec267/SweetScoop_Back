package com.sweetscoop.admin.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sweetscoop.admin.entity.Promotion;

import lombok.Getter;

@Getter
public class PromotionResponse {

    private Integer id;

    private String eventName;

    private String eventContent;
    
    private String imageCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;


    public PromotionResponse(Promotion promotion) {

        this.id = promotion.getId();
        this.eventName = promotion.getEventName();
        this.eventContent = promotion.getEventContent();
        this.imageCode = promotion.getImageCode();
        this.startDate = promotion.getStartDate();
        this.endDate = promotion.getEndDate();
        this.startTime = promotion.getStartTime();
        this.endTime = promotion.getEndTime();

    }

}