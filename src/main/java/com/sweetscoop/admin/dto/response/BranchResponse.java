package com.sweetscoop.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchResponse {
    private Integer id;
    private String branchName;
    private String location;
    // 필요 시 기기 상태(정상/고장 등) 요약을 여기에 포함할 수 있습니다.
}