package com.sweetscoop.coupon.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sweetscoop.coupon.entity.Coupon;

public interface CouponRepository
        extends JpaRepository<Coupon, Integer> {

    /**
     * 회원이 보유한 전체 쿠폰을 최신순으로 조회
     */
    List<Coupon> findByMemberIdOrderByIdDesc(
            Integer memberId
    );

    /**
     * 회원이 현재 사용할 수 있는 쿠폰 조회
     *
     * 조건:
     * 1. 해당 회원의 쿠폰
     * 2. 아직 사용하지 않은 쿠폰
     * 3. 유효기간이 현재 시각 이후인 쿠폰
     */
    List<Coupon>
    findByMemberIdAndIsUsedFalseAndExpiryDateAfterOrderByIdDesc(
            Integer memberId,
            LocalDateTime currentDateTime
    );
}
