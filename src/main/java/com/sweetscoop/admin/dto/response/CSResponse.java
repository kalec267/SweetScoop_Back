package com.sweetscoop.admin.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CSResponse {

    private Integer id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String managerName;
    private String hqManagerName;
    private String answer;

    private LocalDateTime answeredAt;

    
}