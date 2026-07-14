package com.sweetscoop.member.controller;

import com.sweetscoop.member.dto.MemberDto;
import com.sweetscoop.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order/members")//주문/적립 시스템 전용 경로
public class OrderMemberController {

    @Autowired 
    private MemberService memberService;

    @PostMapping("/check-in")
    public ResponseEntity<MemberDto> checkInMember(
    		
            @RequestParam("customerId")
            Integer customerId,
            @RequestParam("phoneNumber")
            String phoneNumber) {
        
        MemberDto result = memberService.processMemberCheckIn(customerId, phoneNumber);
        return ResponseEntity.ok(result);
    }
}
