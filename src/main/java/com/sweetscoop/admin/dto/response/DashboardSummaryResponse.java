package com.sweetscoop.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryResponse {
    private long totalRequests;    // 전체 신청 건수
    private long pendingCount;     // 처리 대기 건수
    private long shippingCount;    // 배송 중 건수
    private long activeBranches;   // 전체 분점 수
}