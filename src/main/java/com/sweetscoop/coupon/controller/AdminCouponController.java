package com.sweetscoop.coupon.controller;

import com.sweetscoop.coupon.dto.CouponCreateRequestDto;
import com.sweetscoop.coupon.dto.CouponDto;
import com.sweetscoop.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminCouponController {
	
    @Autowired
    private CouponService couponService;
    
    @GetMapping
    public ResponseEntity<List<CouponDto>> getCustomerCoupons(@RequestParam("memberId") Integer memberId) {
        return ResponseEntity.ok(couponService.getCouponsByMemberId(memberId));
    }
    
    // 기본 컴백 쿠폰 즉시 발송 API
    @PostMapping("/comeback")
    public ResponseEntity<CouponDto> issueComebackCoupon(@RequestParam("memberId") Integer memberId) {
        return ResponseEntity.ok(couponService.issueComebackCoupon(memberId));
    }

    // 커스텀 쿠폰 생성 및 발급 API
    @PostMapping
    public ResponseEntity<CouponDto> createCoupon(@RequestBody CouponCreateRequestDto requestDto) {
        return ResponseEntity.ok(couponService.createAndIssueCoupon(requestDto));
    }
    
    @PostMapping("/template")
    public ResponseEntity<CouponDto> createTemplate(@RequestBody CouponCreateRequestDto requestDto) {
        return ResponseEntity.ok(couponService.createCouponTemplate(requestDto));
    }

    // 등록된 템플릿 쿠폰을 회원에게 발급 API
    @PostMapping("/issue")
    public ResponseEntity<CouponDto> issueCoupon(
            @RequestParam("couponId") Integer couponId,
            @RequestParam("memberId") Integer memberId) {
        return ResponseEntity.ok(couponService.issueCouponToMember(couponId, memberId));
    }

    // 화면에 보이는 용도의 쿠폰 전체 목록 조회
    @GetMapping("/all")
    public ResponseEntity<List<CouponDto>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }
    
    // 템플릿 쿠폰 삭제 API
    @DeleteMapping("/template/{couponId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable("couponId") Integer couponId) {
        couponService.deleteCouponTemplate(couponId);
        return ResponseEntity.ok().build();
    }

    // ✨ [신규] 발급된 회원 보유 쿠폰 회수/삭제 API
    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteMemberCoupon(@PathVariable("couponId") Integer couponId) {
        couponService.deleteMemberCoupon(couponId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/template/{couponId}")
    public ResponseEntity<CouponDto> updateTemplate(
            @PathVariable("couponId") Integer couponId,
            @RequestBody CouponCreateRequestDto requestDto) {
        return ResponseEntity.ok(couponService.updateCouponTemplate(couponId, requestDto));
    }
}