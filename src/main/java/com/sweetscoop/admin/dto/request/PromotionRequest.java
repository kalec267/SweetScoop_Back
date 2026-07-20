package com.sweetscoop.admin.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionRequest {

    private String eventName;

    private String eventContent;
    
    private String imageCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}