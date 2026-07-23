package com.sweetscoop.admin.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoticeResponse {


    private Integer id;

    private String hqManagerId;
    
    private String hqManagerName;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;


}