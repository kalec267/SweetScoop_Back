package com.sweetscoop.member.dto;

import com.sweetscoop.member.entity.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MemberDto {
    private Integer id;
    private Integer customerId;
    private String phoneNumber;
    private Integer orderCount;
    private Integer point;
    private LocalDateTime createdAt;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.customerId = member.getCustomerId();
        this.phoneNumber = member.getPhoneNumber();
        this.orderCount = member.getOrderCount();
        this.point = member.getPoint();
        this.createdAt = member.getCreatedAt();
    }
}