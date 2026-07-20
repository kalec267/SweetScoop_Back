package com.sweetscoop.coupon.controller;
import com.sweetscoop.coupon.dto.CouponDto;
import com.sweetscoop.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {
    @Autowired private CouponService couponService;
    @GetMapping
    public ResponseEntity<List<CouponDto>> getCustomerCoupons(@RequestParam("memberId") Integer memberId) {
        return ResponseEntity.ok(couponService.getCouponsByMemberId(memberId));
    }
}
