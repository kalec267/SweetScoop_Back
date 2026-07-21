package com.sweetscoop.coupon.service;

import com.sweetscoop.coupon.dto.CouponCreateRequestDto;
import com.sweetscoop.coupon.dto.CouponDto;
import com.sweetscoop.coupon.entity.Coupon;
import com.sweetscoop.coupon.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponService {
	
    @Autowired 
    private CouponRepository couponRepository;
    
    private static final String COMEBACK_COUPON_NAME = "[컴백] 다시 만나 반가워요 3,000원 할인쿠폰";
    private static final Double COMEBACK_DISCOUNT_VALUE = 3000.0;
    private static final int DEFAULT_VALID_DAYS = 30;
    
    public List<CouponDto> getCouponsByMemberId(Integer memberId) {
        return couponRepository.findByMemberIdOrderByIdDesc(memberId).stream().map(CouponDto::new).toList();
    }
    
    public List<CouponDto> getAllCoupons() {
        return couponRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId())) // 최신순 정렬
                .map(CouponDto::new)
                .toList();
    }
    
    @Transactional
    public CouponDto issueComebackCoupon(Integer memberId) {
        if (couponRepository.existsByMemberIdAndNameAndIsUsedFalse(memberId, COMEBACK_COUPON_NAME)) {
            throw new IllegalStateException("이미 발급된 미사용 컴백 쿠폰이 존재합니다.");
        }

        LocalDateTime now = LocalDateTime.now();

        Coupon coupon = Coupon.builder()
                .memberId(memberId)
                .name(COMEBACK_COUPON_NAME)
                .discountValue(COMEBACK_DISCOUNT_VALUE)
                .issueDate(now)
                .expiryDate(now.plusDays(DEFAULT_VALID_DAYS))
                .isUsed(false)
                .build();

        return new CouponDto(couponRepository.save(coupon));
    }

    @Transactional
    public CouponDto createAndIssueCoupon(CouponCreateRequestDto requestDto) {
        LocalDateTime now = LocalDateTime.now();
        
        int validDays = (requestDto.getValidDays() != null) ? requestDto.getValidDays() : DEFAULT_VALID_DAYS;

        Coupon coupon = Coupon.builder()
                .memberId(requestDto.getMemberId())
                .name(requestDto.getName())
                .discountValue(requestDto.getDiscountValue())
                .issueDate(now)
                .expiryDate(now.plusDays(validDays))
                .isUsed(false)
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);
        return new CouponDto(savedCoupon);
    }

    @Transactional
    public CouponDto issueCouponToMember(Integer couponId, Integer memberId) {
        Coupon template = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다. ID: " + couponId));

        LocalDateTime now = LocalDateTime.now();

        Coupon memberCoupon = Coupon.builder()
                .memberId(memberId)
                .name(template.getName())
                .discountValue(template.getDiscountValue())
                .issueDate(now)
                .expiryDate(now.plusDays(DEFAULT_VALID_DAYS))
                .isUsed(false)
                .build();

        return new CouponDto(couponRepository.save(memberCoupon));
    }

    @Transactional
    public CouponDto createCouponTemplate(CouponCreateRequestDto requestDto) {
        Coupon template = Coupon.builder()
                .memberId(null)
                .name(requestDto.getName())
                .discountValue(requestDto.getDiscountValue())
                .build();

        return new CouponDto(couponRepository.save(template));
    }
    
    // 템플릿(미발급) 쿠폰 삭제
    @Transactional
    public void deleteCouponTemplate(Integer couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다. ID: " + couponId));

        if (coupon.getMemberId() != null) {
            throw new IllegalStateException("회원에게 이미 발급된 쿠폰은 템플릿 삭제 메서드로 삭제할 수 없습니다.");
        }

        couponRepository.delete(coupon);
    }

    // ✨ [신규] 발급된 회원 보유 쿠폰 삭제/회수
    @Transactional
    public void deleteMemberCoupon(Integer couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다. ID: " + couponId));

        if (Boolean.TRUE.equals(coupon.getIsUsed())) {
            throw new IllegalStateException("이미 사용 완료된 쿠폰은 회수/삭제할 수 없습니다.");
        }

        couponRepository.delete(coupon);
    }
    
    @Transactional
    public CouponDto updateCouponTemplate(Integer couponId, CouponCreateRequestDto requestDto) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다. ID: " + couponId));

        if (coupon.getMemberId() != null) {
            throw new IllegalStateException("회원에게 이미 발급된 쿠폰은 수정할 수 없습니다.");
        }

        coupon.setName(requestDto.getName());
        coupon.setDiscountValue(requestDto.getDiscountValue());

        return new CouponDto(coupon);
    }
}