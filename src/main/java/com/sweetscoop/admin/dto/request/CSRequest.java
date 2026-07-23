package com.sweetscoop.admin.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CSRequest {

    private String title;

    private String content;

    private String managerId;
    
    private String hqManagerId;

    private String answer;
}