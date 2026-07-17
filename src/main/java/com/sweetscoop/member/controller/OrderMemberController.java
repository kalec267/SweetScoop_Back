package com.sweetscoop.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.member.dto.MemberDto;
import com.sweetscoop.member.dto.MemberRewardRequestDto;
import com.sweetscoop.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order/members")
@RequiredArgsConstructor
public class OrderMemberController {

    private final MemberService memberService;

    /**
     * 기존 회원 조회 또는 신규 회원 자동 가입.
     * 이 단계에서는 결제 전이므로 orderCount를 증가시키지 않는다.
     */
    @PostMapping("/check-in")
    public ResponseEntity<MemberDto> checkInMember(
            @RequestParam Integer customerId,
            @RequestParam String phoneNumber) {

        return ResponseEntity.ok(
                memberService.processMemberCheckIn(customerId, phoneNumber)
        );
    }

    /** 결제 승인 성공 후에만 주문 횟수와 포인트를 반영한다. */
    @PostMapping("/reward")
    public ResponseEntity<MemberDto> rewardAfterPayment(
            @RequestBody MemberRewardRequestDto request) {

        return ResponseEntity.ok(
                memberService.rewardAfterPayment(
                        request.getMemberId(),
                        request.getPaymentAmount()
                )
        );
    }
}