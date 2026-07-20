package com.sweetscoop.payment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sweetscoop.coupon.dto.CouponDto;
import com.sweetscoop.coupon.entity.Coupon;
import com.sweetscoop.coupon.repository.CouponRepository;
import com.sweetscoop.member.entity.Member;
import com.sweetscoop.member.repository.MemberRepository;
import com.sweetscoop.payment.dto.MemberBenefitResponseDTO;
import com.sweetscoop.payment.dto.PaymentCalculationRequestDTO;
import com.sweetscoop.payment.dto.PaymentCalculationResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentCalculationService {

    private static final int POINT_USAGE_UNIT = 500;

    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;

    /**
     * 전화번호로 회원의 포인트와 사용 가능한 쿠폰 조회
     */
    public MemberBenefitResponseDTO getMemberBenefits(
            String phoneNumber
    ) {

        String normalizedPhoneNumber =
                normalizePhoneNumber(phoneNumber);

        Member member = memberRepository
                .findByPhoneNumber(normalizedPhoneNumber)
                .orElse(null);

        if (member == null) {
            return MemberBenefitResponseDTO.builder()
                    .member(false)
                    .memberId(null)
                    .phoneNumber(normalizedPhoneNumber)
                    .point(0)
                    .coupons(List.of())
                    .build();
        }

        List<CouponDto> availableCoupons =
                couponRepository
                        .findByMemberIdAndIsUsedFalseAndExpiryDateAfterOrderByIdDesc(
                                member.getId(),
                                LocalDateTime.now()
                        )
                        .stream()
                        .map(CouponDto::new)
                        .toList();

        return MemberBenefitResponseDTO.builder()
                .member(true)
                .memberId(member.getId())
                .phoneNumber(member.getPhoneNumber())
                .point(
                        member.getPoint() == null
                                ? 0
                                : member.getPoint()
                )
                .coupons(availableCoupons)
                .build();
    }

    /**
     * 쿠폰과 포인트를 적용한 결제 금액 계산
     */
    public PaymentCalculationResponseDTO calculate(
            PaymentCalculationRequestDTO request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "결제 계산 요청이 없습니다."
            );
        }

        int originalAmount =
                requirePositiveAmount(
                        request.getOriginalAmount()
                );

        int pointUsed =
                request.getPointUsed() == null
                        ? 0
                        : request.getPointUsed();

        validatePointUnit(pointUsed);

        Member member = null;
        int availablePoint = 0;
        int couponDiscount = 0;

        /*
         * 전화번호가 입력된 경우 회원 조회
         */
        if (
                request.getPhoneNumber() != null
                        && !request.getPhoneNumber().isBlank()
        ) {

            String phoneNumber =
                    normalizePhoneNumber(
                            request.getPhoneNumber()
                    );

            member = memberRepository
                    .findByPhoneNumber(phoneNumber)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "회원 정보를 찾을 수 없습니다."
                            )
                    );

            availablePoint =
                    member.getPoint() == null
                            ? 0
                            : member.getPoint();
        }

        /*
         * 포인트 사용 검증
         */
        if (member == null && pointUsed > 0) {
            throw new IllegalArgumentException(
                    "비회원은 포인트를 사용할 수 없습니다."
            );
        }

        if (pointUsed > availablePoint) {
            throw new IllegalArgumentException(
                    "보유 포인트보다 많이 사용할 수 없습니다."
            );
        }

        /*
         * 쿠폰 검증 및 할인 금액 계산
         */
        if (request.getCouponId() != null) {

            if (member == null) {
                throw new IllegalArgumentException(
                        "비회원은 쿠폰을 사용할 수 없습니다."
                );
            }

            Coupon coupon = couponRepository
                    .findById(request.getCouponId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "쿠폰을 찾을 수 없습니다."
                            )
                    );

            validateCoupon(
                    coupon,
                    member.getId()
            );

            couponDiscount =
                    coupon.getDiscountValue() == null
                            ? 0
                            : coupon.getDiscountValue()
                                    .intValue();
        }

        /*
         * 쿠폰 할인은 주문 금액을 초과할 수 없음
         */
        int appliedCouponDiscount =
                Math.min(
                        couponDiscount,
                        originalAmount
                );

        /*
         * 쿠폰 적용 후 남은 금액
         */
        int remainingAmountAfterCoupon =
                Math.max(
                        0,
                        originalAmount
                                - appliedCouponDiscount
                );

        /*
         * 사용할 수 있는 최대 포인트를
         * 500원 단위로 내림 처리
         *
         * 예:
         * 남은 금액 1,700원 → 최대 1,500P
         * 남은 금액   400원 → 최대 0P
         */
        int maximumPointByPayment =
                (
                    remainingAmountAfterCoupon
                    / POINT_USAGE_UNIT
                ) * POINT_USAGE_UNIT;

        /*
         * 보유 포인트 또한 500원 단위까지만 사용 가능
         *
         * 예:
         * 보유 포인트 5,700P → 최대 5,500P
         */
        int maximumPointByBalance =
                (
                    availablePoint
                    / POINT_USAGE_UNIT
                ) * POINT_USAGE_UNIT;

        int maximumUsablePoint =
                Math.min(
                        maximumPointByPayment,
                        maximumPointByBalance
                );

        /*
         * 요청한 포인트가 결제 금액 기준 최대치를 초과하면
         * 임의로 변경하지 않고 오류를 발생시킨다.
         */
        if (pointUsed > maximumUsablePoint) {
            throw new IllegalArgumentException(
                    "사용 가능한 최대 포인트는 "
                            + maximumUsablePoint
                            + "P입니다."
            );
        }

        int appliedPoint = pointUsed;

        int totalDiscount =
                appliedCouponDiscount
                        + appliedPoint;

        int finalAmount =
                originalAmount
                        - totalDiscount;

        /*
         * Toss 결제를 진행하려면 최종 금액은
         * 최소 1원 이상이어야 한다.
         */
        if (finalAmount <= 0) {
            throw new IllegalArgumentException(
                    "할인 적용 후 결제 금액은 "
                            + "1원 이상이어야 합니다."
            );
        }

        return PaymentCalculationResponseDTO.builder()
                .originalAmount(originalAmount)
                .pointDiscount(appliedPoint)
                .couponDiscount(
                        appliedCouponDiscount
                )
                .totalDiscount(totalDiscount)
                .finalAmount(finalAmount)
                .remainingPoint(
                        availablePoint
                                - appliedPoint
                )
                .build();
    }

    /**
     * 결제 시점 쿠폰 재검증
     */
    public Coupon getValidatedCoupon(
            Integer couponId,
            Integer memberId
    ) {

        if (couponId == null) {
            return null;
        }

        Coupon coupon = couponRepository
                .findById(couponId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "쿠폰을 찾을 수 없습니다."
                        )
                );

        validateCoupon(coupon, memberId);

        return coupon;
    }

    /**
     * 포인트 사용 단위 검증
     */
    private void validatePointUnit(
            int pointUsed
    ) {

        if (pointUsed < 0) {
            throw new IllegalArgumentException(
                    "사용 포인트는 0 이상이어야 합니다."
            );
        }

        if (pointUsed % POINT_USAGE_UNIT != 0) {
            throw new IllegalArgumentException(
                    "포인트는 "
                            + POINT_USAGE_UNIT
                            + "원 단위로만 사용할 수 있습니다."
            );
        }
    }

    /**
     * 쿠폰 사용 가능 여부 검증
     */
    private void validateCoupon(
            Coupon coupon,
            Integer memberId
    ) {

        if (memberId == null) {
            throw new IllegalArgumentException(
                    "회원 정보가 없습니다."
            );
        }

        if (
                coupon.getMemberId() == null
                        || !coupon.getMemberId()
                                .equals(memberId)
        ) {
            throw new IllegalArgumentException(
                    "해당 회원의 쿠폰이 아닙니다."
            );
        }

        if (Boolean.TRUE.equals(coupon.getIsUsed())) {
            throw new IllegalArgumentException(
                    "이미 사용한 쿠폰입니다."
            );
        }

        if (
                coupon.getExpiryDate() != null
                        && coupon.getExpiryDate()
                                .isBefore(
                                        LocalDateTime.now()
                                )
        ) {
            throw new IllegalArgumentException(
                    "사용 기간이 만료된 쿠폰입니다."
            );
        }
    }

    /**
     * 주문 금액 검증
     */
    private int requirePositiveAmount(
            Integer amount
    ) {

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException(
                    "결제 금액이 올바르지 않습니다."
            );
        }

        return amount;
    }

    /**
     * 전화번호 숫자만 남기기
     */
    private String normalizePhoneNumber(
            String phoneNumber
    ) {

        if (phoneNumber == null) {
            return "";
        }

        return phoneNumber.replaceAll(
                "[^0-9]",
                ""
        );
    }
}
