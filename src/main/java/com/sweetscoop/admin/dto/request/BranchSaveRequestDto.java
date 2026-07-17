package com.sweetscoop.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BranchSaveRequestDto {
    private String branchName;  // 지점명
    private String location;    // 주소/위치
}