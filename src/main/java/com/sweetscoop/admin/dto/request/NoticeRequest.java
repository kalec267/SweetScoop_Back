package com.sweetscoop.admin.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeRequest {


    private String hqManagerId;

    private String title;

    private String content;
    
    private LocalDateTime updateAt;

}