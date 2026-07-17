package com.sweetscoop.member.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sweetscoop.coupon.entity.Coupon;
import com.sweetscoop.coupon.repository.CouponRepository;
import com.sweetscoop.member.dto.MemberDto;
import com.sweetscoop.member.entity.Member;
import com.sweetscoop.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private static final int REWARD_ORDER_THRESHOLD = 5;
    private static final double REWARD_COUPON_VALUE = 5000.0;
    private static final double POINT_RATE = 0.05;

    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;

    public List<MemberDto> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberDto::new)
                .toList();
    }

    /**
     * 전화번호 확인 단계.
     * 기존 회원은 조회만 하고, 신규 번호는 customer_id=1 회원으로 자동 가입한다.
     * 결제 전이므로 order_count와 point는 변경하지 않는다.
     */
    @Transactional
    public MemberDto processMemberCheckIn(Integer customerId, String phoneNumber) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);

        if (normalizedPhone.length() < 10) {
            throw new IllegalArgumentException("올바른 전화번호를 입력해주세요.");
        }

        return memberRepository.findByPhoneNumber(normalizedPhone)
                .map(MemberDto::new)
                .orElseGet(() -> createMember(1, normalizedPhone));
    }

    private MemberDto createMember(Integer customerId, String phoneNumber) {
        Member member = new Member();
        member.setCustomerId(customerId);
        member.setPhoneNumber(phoneNumber);
        member.setOrderCount(0);
        member.setPoint(0);
        member.setCreatedAt(LocalDateTime.now());

        Member savedMember = memberRepository.save(member);

        Coupon welcomeCoupon = new Coupon();
        welcomeCoupon.setMemberId(savedMember.getId());
        welcomeCoupon.setName("신규 가입 축하 3,000원 할인권");
        welcomeCoupon.setIssueDate(LocalDateTime.now());
        welcomeCoupon.setExpiryDate(LocalDateTime.now().plusMonths(1));
        welcomeCoupon.setDiscountValue(3000.0);
        welcomeCoupon.setIsUsed(false);
        couponRepository.save(welcomeCoupon);

        return new MemberDto(savedMember);
    }

    /**
     * 토스 결제 승인 성공 후 호출한다.
     * 이 메서드에서만 order_count를 1 증가시키고 결제금액의 5%를 적립한다.
     */
    @Transactional
    public MemberDto rewardAfterPayment(Integer memberId, Integer paymentAmount) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID가 필요합니다.");
        }
        if (paymentAmount == null || paymentAmount <= 0) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        int currentOrderCount = member.getOrderCount() == null ? 0 : member.getOrderCount();
        int currentPoint = member.getPoint() == null ? 0 : member.getPoint();
        int nextOrderCount = currentOrderCount + 1;
        int earnedPoint = (int) Math.floor(paymentAmount * POINT_RATE);

        member.setOrderCount(nextOrderCount);
        member.setPoint(currentPoint + earnedPoint);

        if (nextOrderCount % REWARD_ORDER_THRESHOLD == 0) {
            Coupon rewardCoupon = new Coupon();
            rewardCoupon.setMemberId(member.getId());
            rewardCoupon.setName(String.format("단골 감사 %d회 주문 기념 할인권", nextOrderCount));
            rewardCoupon.setIssueDate(LocalDateTime.now());
            rewardCoupon.setExpiryDate(LocalDateTime.now().plusMonths(3));
            rewardCoupon.setDiscountValue(REWARD_COUPON_VALUE);
            rewardCoupon.setIsUsed(false);
            couponRepository.save(rewardCoupon);
        }

        return new MemberDto(member);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}