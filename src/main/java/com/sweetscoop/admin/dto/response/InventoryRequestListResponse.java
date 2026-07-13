package com.sweetscoop.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryRequestListResponse {
    private Integer requestId;     // 신청 번호 (#REQ-XXXX 에 매핑할 ID)
    private String branchName;     // 분점명 (Branch에서 가져옴)
    private String requestMenu;    // 신청 메뉴명 (Item에서 가져옴)
    private Integer quantity;      // 수량
    private String status;         // 상태 (대기중/배송중/완료/반려 등의 종합 상태 표기용)
    
    // 필요 시 날짜 필드(requestDate) 추가 가능
}