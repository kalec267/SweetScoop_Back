package com.sweetscoop.member.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
		return memberRepository.findAll().stream().map(MemberDto::new).toList();
	}

	/*
	 * 전화번호 입력 시: 기존 회원 조회 또는 신규 회원 자동 가입
	 *
	 * 이 단계에서는 주문 횟수와 포인트를 변경하지 않음
	 */
	@Transactional
	public MemberDto processMemberCheckIn(Integer customerId, String phoneNumber) {

		String normalizedPhone = phoneNumber.replaceAll("[^0-9]", "");

		Optional<Member> existingMember = memberRepository.findByPhoneNumber(normalizedPhone);

		if (existingMember.isPresent()) {

			// 기존 회원은 조회만 하고 아무 값도 변경하지 않음
			Member member = existingMember.get();

			return new MemberDto(member);

		} else {

			// 신규 회원 자동 가입
			Member newMember = new Member();

			newMember.setCustomerId(1); // 회원 유형
			newMember.setPhoneNumber(normalizedPhone);

			// 아직 결제 전이므로 주문 횟수는 0
			newMember.setOrderCount(0);

			newMember.setPoint(0);
			newMember.setCreatedAt(LocalDateTime.now());

			Member savedMember = memberRepository.save(newMember);

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
	}

	/*
	 * 신규 회원 자동 가입
	 */
	private MemberDto createMember(Integer customerId, String phoneNumber) {

		if (customerId == null) {
			throw new IllegalArgumentException("고객 ID가 필요합니다.");
		}

		Member newMember = new Member();

		newMember.setCustomerId(customerId);
		newMember.setPhoneNumber(phoneNumber);
		newMember.setOrderCount(0);
		newMember.setPoint(0);
		newMember.setCreatedAt(LocalDateTime.now());

		Member savedMember = memberRepository.save(newMember);

		/*
		 * 신규 회원 웰컴 쿠폰 발급
		 */
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

	/*
	 * 결제 성공 후 호출
	 */
	@Transactional
	public MemberDto rewardAfterPayment(Integer memberId, Integer paymentAmount) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

		int currentOrderCount = member.getOrderCount() == null ? 0 : member.getOrderCount();

		int currentPoint = member.getPoint() == null ? 0 : member.getPoint();

		int nextOrderCount = currentOrderCount + 1;

		int earnedPoint = (int) Math.floor(paymentAmount * 0.05);

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