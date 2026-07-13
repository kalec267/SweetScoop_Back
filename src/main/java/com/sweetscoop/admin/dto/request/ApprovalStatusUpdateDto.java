package com.sweetscoop.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApprovalStatusUpdateDto {
    private String status;       // "승인완료" 또는 "반려"
    private String hqManagerId;
}