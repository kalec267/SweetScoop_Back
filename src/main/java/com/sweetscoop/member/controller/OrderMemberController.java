package com.sweetscoop.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sweetscoop.member.dto.MemberDto;
import com.sweetscoop.member.dto.MemberRewardRequestDto;
import com.sweetscoop.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order/members")
@RequiredArgsConstructor
public class OrderMemberController {

    private final MemberService memberService;

    /*
     * 기존 회원 조회 또는 신규 회원 자동 가입
     */
    @PostMapping("/check-in")
    public ResponseEntity<MemberDto> checkInMember(
            @RequestParam Integer customerId,
            @RequestParam String phoneNumber) {

        MemberDto result =
                memberService.processMemberCheckIn(
                        customerId,
                        phoneNumber
                );

        return ResponseEntity.ok(result);
    }

    /*
     * 결제 성공 후 포인트 적립
     */
    @PostMapping("/reward")
    public ResponseEntity<MemberDto> rewardAfterPayment(
            @RequestBody MemberRewardRequestDto request) {

        MemberDto result =
                memberService.rewardAfterPayment(
                        request.getMemberId(),
                        request.getPaymentAmount()
                );

        return ResponseEntity.ok(result);
    }
}