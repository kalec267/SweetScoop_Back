package com.sweetscoop.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberRewardRequestDto {
    private Integer memberId;
    private Integer orderId;
    private Integer paymentAmount;
}