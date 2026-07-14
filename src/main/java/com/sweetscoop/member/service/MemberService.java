package com.sweetscoop.member.service;

import com.sweetscoop.coupon.entity.Coupon;
import com.sweetscoop.coupon.repository.CouponRepository;
import com.sweetscoop.member.dto.MemberDto;
import com.sweetscoop.member.entity.Member;
import com.sweetscoop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CouponRepository couponRepository;
    
    //5회 주물 할때마다 발급
    private static final int REWARD_ORDER_THRESHOLD = 5;
    private static final double REWARD_COUPON_VALUE = 5000.0;
    
    // 부모 트랜잭션 설정을 따라 readonly로 작동하므로 안전하고 빠릅니다.
    public List<MemberDto> getAllMembers() {
        return memberRepository.findAll().stream().map(MemberDto::new).toList();
    }
    
    @Transactional
    public MemberDto processMemberCheckIn(Integer customerId, String phoneNumber) {
        
        // 1. 이미 존재하는 전화번호인지 DB에서 조회
        Optional<Member> existingMember = memberRepository.findByPhoneNumber(phoneNumber);

        if (existingMember.isPresent()) {
            // [CASE A] 이미 존재하는 회원일 경우 -> 누적 주문 횟수만 1 증가
            Member member = existingMember.get();
            member.setOrderCount(member.getOrderCount() + 1);
            
            // 💡 [단골 리워드] 누적 주문이 5회, 10회, 15회 등 Threshold 배수가 될 때마다 쿠폰 발급
            if (member.getOrderCount() % REWARD_ORDER_THRESHOLD == 0) {
                Coupon rewardCoupon = new Coupon();
                rewardCoupon.setMemberId(member.getId());
                rewardCoupon.setName(String.format("단골 감사 %d회 주문 기념 할인권", member.getOrderCount()));
                rewardCoupon.setIssueDate(LocalDateTime.now());
                rewardCoupon.setExpiryDate(LocalDateTime.now().plusMonths(3)); // 3개월 유효
                rewardCoupon.setDiscountValue(REWARD_COUPON_VALUE);
                rewardCoupon.setIsUsed(false);
                
                couponRepository.save(rewardCoupon);
            }

            return new MemberDto(member);
        } else {
            // [CASE B] 신규 회원일 경우 -> 회원 가입 + 웰컴 쿠폰 발급
            Member newMember = new Member();
            newMember.setCustomerId(customerId);
            newMember.setPhoneNumber(phoneNumber);
            newMember.setOrderCount(1); // 첫 주문
            newMember.setPoint(0);
            newMember.setCreatedAt(LocalDateTime.now());
            
            // 하단 쿠폰 매핑을 위해 즉시 DB에 반영 후 영속화된 ID 획득
            Member savedMember = memberRepository.save(newMember);

            // 웰컴 쿠폰 발급
            Coupon welcomeCoupon = new Coupon();
            welcomeCoupon.setMemberId(savedMember.getId());
            welcomeCoupon.setName("신규 가입 축하 3,000원 할인권");
            welcomeCoupon.setIssueDate(LocalDateTime.now());
            welcomeCoupon.setExpiryDate(LocalDateTime.now().plusMonths(1)); // 1개월 유효
            welcomeCoupon.setDiscountValue(3000.0);
            welcomeCoupon.setIsUsed(false);
            
            couponRepository.save(welcomeCoupon);

            return new MemberDto(savedMember);
        }
    }
}