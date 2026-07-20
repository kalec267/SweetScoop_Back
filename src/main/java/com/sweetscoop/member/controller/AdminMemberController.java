package com.sweetscoop.member.controller;
import com.sweetscoop.member.dto.MemberDto;
import com.sweetscoop.member.entity.Member;
import com.sweetscoop.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/members")
public class AdminMemberController {
	
    @Autowired 
    private MemberService memberService;
    
    @GetMapping
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }
    
    @PostMapping("/order/members/check-in") 
    public ResponseEntity<MemberDto> checkInMember(
            @RequestParam("customerId") Integer customerId,
            @RequestParam("phoneNumber") String phoneNumber) {
        
        MemberDto result = memberService.processMemberCheckIn(customerId, phoneNumber);
        return ResponseEntity.ok(result);
    }
}